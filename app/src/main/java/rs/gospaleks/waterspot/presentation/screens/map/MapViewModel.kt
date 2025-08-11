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
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTrackingUseCase: LocationTrackingUseCase,
    private val getAllSpotsUseCase: GetAllSpotsUseCase,
    private val getSpotDetailsUseCase: GetSpotDetailsUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var hasCenteredMap = false

    init {
        observeSpots()
    }

    private fun observeLocation() = viewModelScope.launch {
        locationTrackingUseCase.currentLocation.collect { location ->
            uiState = uiState.copy(location = location)
        }
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
            sheetMode = BottomSheetMode.DETAILS,
            isModalOpen = false,
            selectedSpotId = null,
            selectedSpotDetails = null,
            isSpotDetailsLoading = false,
            error = null,
        )
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

    fun openReview() {
        uiState = uiState.copy(sheetMode = BottomSheetMode.REVIEW)
    }

    fun openDetails() {
        uiState = uiState.copy(sheetMode = BottomSheetMode.DETAILS)
    }

    // Location
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        locationTrackingUseCase.startTracking()
        observeLocation()

        uiState = uiState.copy(
            isLocationPermissionGranted = true
        )
    }

    fun stopLocationUpdates() {
        locationTrackingUseCase.stopTracking()
    }

    fun shouldCenterMap(): Boolean {
        return !hasCenteredMap
    }

    fun setCentered() {
        hasCenteredMap = true
    }
}
