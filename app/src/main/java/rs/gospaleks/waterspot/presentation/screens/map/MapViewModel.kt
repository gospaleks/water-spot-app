package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class MapViewModel(application: Application) : AndroidViewModel(application) {
    var uiState by mutableStateOf(MapUiState())
        private set

    private var hasCenteredMap = false

    private val context = application

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val locationRequest = LocationRequest.Builder(3000L)
        .setMinUpdateIntervalMillis(2000L)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let {
                val newLocation = LatLng(it.latitude, it.longitude)
                uiState = uiState.copy(
                    location = newLocation,
                )
            }
        }
    }

    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            uiState = uiState.copy(
                properties = uiState.properties.copy(isMyLocationEnabled = true),
                isLocationPermissionGranted = true
            )
        } else {
            uiState = uiState.copy(
                properties = uiState.properties.copy(isMyLocationEnabled = false),
                isLocationPermissionGranted = false
            )
        }
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun shouldCenterMap(): Boolean {
        return !hasCenteredMap
    }

    fun setCentered() {
        hasCenteredMap = true
    }
}