package rs.gospaleks.waterspot.domain.use_case.location

import android.Manifest
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class LocationTrackingUseCase @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) {
    private val _currentLocation = MutableStateFlow<LatLng?>(null)

    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()

    private var activeTrackers = 0
    private var currentLocationTokenSource: CancellationTokenSource? = null

    private val locationRequest = LocationRequest.Builder(3000L)
        .setMinUpdateIntervalMillis(2000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                _currentLocation.value = LatLng(it.latitude, it.longitude)
            }
        }
    }

    @Synchronized
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking() {
        if (activeTrackers == 0) {
            // Trazi svezu (current) lokaciju umesto poslednje poznate
            currentLocationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                currentLocationTokenSource!!.token
            ).addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = LatLng(it.latitude, it.longitude)
                }
            }.addOnFailureListener { e ->
                Log.w("LocationTrackingUseCase", "getCurrentLocation failed", e)
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("LocationTrackingUseCase", "START tracking")
        }
        activeTrackers++
        Log.d("LocationTrackingUseCase", "Active trackers: $activeTrackers")
    }

    fun stopTracking() {
        activeTrackers--
        Log.d("LocationTrackingUseCase", "Active trackers: $activeTrackers")

        if (activeTrackers <= 0) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            // Otkaži eventualan pending getCurrentLocation
            currentLocationTokenSource?.cancel()
            currentLocationTokenSource = null
            activeTrackers = 0
            Log.d("LocationTrackingUseCase", "STOP tracking")
        }
    }
}