package rs.gospaleks.waterspot.domain.use_case

import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
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
    @RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startTracking() {
        if (activeTrackers == 0) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _currentLocation.value = LatLng(it.latitude, it.longitude)
                }
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
            activeTrackers = 0
            Log.d("LocationTrackingUseCase", "STOP tracking")
        }
    }
}