package rs.gospaleks.waterspot.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import rs.gospaleks.waterspot.common.NotificationHelper
import rs.gospaleks.waterspot.domain.use_case.NearbyTrackingUseCase
import rs.gospaleks.waterspot.data.local.LocationTrackingPreferences

@AndroidEntryPoint
class LocationTrackingService : Service() {
    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    @Inject lateinit var fusedLocationClient: FusedLocationProviderClient
    @Inject lateinit var nearbyTrackingUseCase: NearbyTrackingUseCase
    @Inject lateinit var locationPrefs: LocationTrackingPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastNotifiedSpotIds: Set<String> = emptySet()
    private var nearbyRadiusMeters: Double = 100.0

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val INTERVAL_MS = 60_000L
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
            val result = nearbyTrackingUseCase(lat, lng, radius)
            result.onSuccess { spotsWithUsers ->
                val ids = spotsWithUsers.map { it.spot.id }.toSet()
                if (ids.isNotEmpty() && ids != lastNotifiedSpotIds) {
                    val title = "Nearby water spot"
                    val within = radius.toInt()
                    val message = if (spotsWithUsers.size == 1) {
                        "There is 1 spot within ${within}m."
                    } else {
                        "There are ${spotsWithUsers.size} spots within ${within}m."
                    }
                    notificationHelper.showNotification(
                        notificationId = NEARBY_NOTIFICATION_ID,
                        title = title,
                        message = message
                    )
                    lastNotifiedSpotIds = ids
                }
            }.onFailure { e ->
                Log.e(TAG, "Nearby check failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        serviceScope.cancel()
        Log.d(TAG, "Servis zaustavljen")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
