package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsUseCase
import rs.gospaleks.waterspot.domain.use_case.GetSpotDetailsUseCase
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val getAllSpotsUseCase: GetAllSpotsUseCase,
    private val getSpotDetailsUseCase: GetSpotDetailsUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var hasCenteredMap = false

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


    init {
        observeSpots()
    }

    private fun observeSpots() = viewModelScope.launch {
        uiState = uiState.copy(isLoadingSpots = true)

        getAllSpotsUseCase().collect { result ->
            result
                .onSuccess { spots ->
                    uiState = uiState.copy(
                        spots = spots,
                        isLoadingSpots = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoadingSpots = false,
                        error = error.message ?: "Unknown error"
                    )
                }
        }
    }

    fun onMarkerClick(spotId: String) {
        uiState = uiState.copy(
            isModalOpen = true,
            selectedSpotId = spotId,
            selectedSpotDetails = null,
            isSpotDetailsLoading = true,
            error = null,
        )
    }

    fun loadSpotDetails(spotId: String) = viewModelScope.launch {
        getSpotDetailsUseCase(spotId).onSuccess { spotDetails ->
            uiState = uiState.copy(
                isSpotDetailsLoading = false,
                isModalOpen = true,
                selectedSpotDetails = spotDetails,
                error = null,
            )
        }.onFailure { error ->
            uiState = uiState.copy(
                isSpotDetailsLoading = false,
                isModalOpen = false,
                selectedSpotDetails = null,
                error = error.message ?: "Unknown error"
            )
        }
    }

    fun dismissBottomSheet() {
        uiState = uiState.copy(
            isModalOpen = false,
            isSpotDetailsLoading = false,
            selectedSpotDetails = null,
            error = null
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        uiState = uiState.copy(
            isLocationPermissionGranted = true
        )
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
