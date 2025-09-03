package rs.gospaleks.waterspot.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.common.NotificationHelper
import rs.gospaleks.waterspot.data.local.LocationTrackingPreferences
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.use_case.user.GetUsersWithLocationSharingInRadiusUseCase
import rs.gospaleks.waterspot.domain.use_case.location.NearbyTrackingUseCase
import rs.gospaleks.waterspot.domain.use_case.user.SetUserLocationUseCase

@AndroidEntryPoint
class LocationTrackingService : Service() {
    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var nearbyTrackingUseCase: NearbyTrackingUseCase
    @Inject lateinit var getUsersWithLocationSharingInRadiusUseCase: GetUsersWithLocationSharingInRadiusUseCase
    @Inject lateinit var setUserLocationUseCase: SetUserLocationUseCase
    @Inject lateinit var locationPrefs: LocationTrackingPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastNotifiedSpotIds: Set<String> = emptySet()
    private var lastNotifiedUserIds: Set<String> = emptySet()
    private var nearbyRadiusMeters: Double = 100.0

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val INTERVAL_MS = 30_000L
        private const val TAG = "LocationTrackingService"
        private const val NEARBY_NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)

        // Observe user-configured nearby radius
        serviceScope.launch {
            locationPrefs.nearbyRadiusMeters.collectLatest { radius ->
                nearbyRadiusMeters = radius.toDouble()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Inicijalna notifikacija koja se prikazuje kada servis pocne da radi
        val notification = notificationHelper.createPersistentNotification(
            title = "Location tracking",
            message = "Location tracking service is running. You will receive notification when a spot is detected.",
        )

        startForeground(NOTIFICATION_ID, notification)

        runnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "Servis aktivan - proveravam okolinu")

                checkNearbyAndNotify()

                handler.postDelayed(this, INTERVAL_MS)
            }
        }
        handler.post(runnable)

        return START_STICKY
    }

    private fun checkNearbyAndNotify() {
        val fineGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            Log.w(TAG, "Location permission not granted. Skipping nearby check.")
            return
        }

        // Always request a fresh current location
        val tokenSource = CancellationTokenSource()
        fusedLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
            .addOnSuccessListener { current ->
                if (current != null) {
                    triggerNearbyCheck(current.latitude, current.longitude)
                } else {
                    Log.w(TAG, "Could not obtain current location.")
                }
            }
            .addOnFailureListener { e -> Log.e(TAG, "getCurrentLocation failed", e) }
    }

    private fun triggerNearbyCheck(lat: Double, lng: Double) {
        serviceScope.launch {
            val radius = nearbyRadiusMeters

            // Update user location on server
            setUserLocationUseCase(lat, lng)
                .onFailure { e -> Log.w(TAG, "Failed to update user location", e) }

            // Fetch users first so we can notify even without spots
            val usersInRadius = try {
                getUsersWithLocationSharingInRadiusUseCase(lat, lng, radius)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load users in radius", e)
                emptyList()
            }

            // Get nearby spots
            val spotsResult = nearbyTrackingUseCase(lat, lng, radius)
            val spotsWithUsers = spotsResult.getOrElse { e ->
                Log.w(TAG, "Failed to load spots in radius", e)
                emptyList()
            }

            val spotIds = spotsWithUsers.map { it.spot.id }.toSet()
            val userIds = usersInRadius.map { it.id }.toSet()

            // Decide whether to notify: if any content exists and either set changed
            val hasContent = spotIds.isNotEmpty() || userIds.isNotEmpty()
            val changed = (spotIds != lastNotifiedSpotIds) || (userIds != lastNotifiedUserIds)

            if (!hasContent || !changed) return@launch

            val within = radius.toInt()
            val spotsCount = spotsWithUsers.size
            val usersCount = usersInRadius.size

            // Compose title
            val title = when {
                spotsCount > 0 && usersCount > 0 -> "Nearby spots and people"
                spotsCount > 0 -> "Nearby water spots"
                else -> "People nearby"
            }

            // Nearest spot line first (if any)
            val nearestLine = if (spotsCount > 0) {
                val nearest = spotsWithUsers.minByOrNull {
                    distanceMeters(lat, lng, it.spot.latitude, it.spot.longitude)
                }
                nearest?.let {
                    val d = distanceMeters(lat, lng, it.spot.latitude, it.spot.longitude).toInt()
                    val s = it.spot
                    "Nearest: ${formatSpotType(s.type)} • ${formatCleanliness(s.cleanliness)} • ~${d}m away"
                } ?: ""
            } else ""

            // Spots header and type breakdown
            val spotsHeader = if (spotsCount > 0) {
                if (spotsCount == 1) {
                    "There is 1 spot within ${within}m."
                } else {
                    "There are $spotsCount spots within ${within}m."
                }
            } else ""

            val typeLine = if (spotsCount > 0) {
                val typeSummary = spotsWithUsers
                    .groupingBy { it.spot.type }
                    .eachCount()
                    .entries
                    .sortedByDescending { it.value }
                    .joinToString(
                        separator = ", ",
                        transform = { (type, count) -> "${formatSpotType(type)} x${count}" }
                    )
                if (typeSummary.isNotBlank()) "Types: $typeSummary" else ""
            } else ""

            // Users line (standalone if no spots, or as an extra line if spots exist)
            val usersLine = if (usersCount > 0) {
                val names = usersInRadius.map { u -> formatDisplayName(u) }
                val displayNames = names.take(3).joinToString(", ")
                val suffix = if (usersCount > 3) ", …" else ""
                if (spotsCount > 0) {
                    // As an additional line under spots info
                    if (usersCount == 1) "Also nearby: 1 user (${displayNames}${suffix})."
                    else "Also nearby: $usersCount users (${displayNames}${suffix})."
                } else {
                    // Users-only notification
                    if (usersCount == 1) "Nearby: 1 user (${displayNames}${suffix})."
                    else "Nearby: $usersCount users (${displayNames}${suffix})."
                }
            } else ""

            // Order: nearest spot first, then spots header, types, then users
            val message = listOf(nearestLine, spotsHeader, typeLine, usersLine)
                .filter { it.isNotBlank() }
                .joinToString("\n")

            notificationHelper.showNotification(
                notificationId = NEARBY_NOTIFICATION_ID,
                title = title,
                message = message
            )

            // Update last-notified fingerprints
            lastNotifiedSpotIds = spotIds
            lastNotifiedUserIds = userIds
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        serviceScope.cancel()
        Log.d(TAG, "Servis zaustavljen")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    private fun formatSpotType(type: SpotTypeEnum): String = when (type) {
        SpotTypeEnum.WELL -> "Well"
        SpotTypeEnum.PUBLIC -> "Public"
        SpotTypeEnum.SPRING -> "Spring"
        SpotTypeEnum.OTHER -> "Other"
    }

    private fun formatCleanliness(cleanliness: CleanlinessLevelEnum): String = when (cleanliness) {
        CleanlinessLevelEnum.CLEAN -> "clean"
        CleanlinessLevelEnum.MODERATE -> "moderate"
        CleanlinessLevelEnum.DIRTY -> "dirty"
    }

    private fun formatDisplayName(user: User): String {
        val name = user.fullName.trim()
        if (name.isNotEmpty()) return name
        val email = user.email.trim()
        if (email.isNotEmpty()) return email.substringBefore('@')
        return "User"
    }
}
