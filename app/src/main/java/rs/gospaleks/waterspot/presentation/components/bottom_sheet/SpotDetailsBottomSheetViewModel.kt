package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.SpotDetails
import rs.gospaleks.waterspot.domain.use_case.GetSpotDetailsUseCase
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase

data class SpotDetailsBottomSheetUiState(
    val userLocation: LatLng? = null,
    val sheetMode: BottomSheetMode = BottomSheetMode.DETAILS,
    val isModalOpen: Boolean = false,
    val isSpotDetailsLoading: Boolean = false,
    val selectedSpotDetails: SpotDetails? = null,
    val selectedSpotId: String? = null,
)

enum class BottomSheetMode { DETAILS, REVIEW }

@HiltViewModel
class SpotDetailsBottomSheetViewModel @Inject constructor(
    private val getSpotDetailsUseCase: GetSpotDetailsUseCase,
    private val locationTrackingUseCase: LocationTrackingUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(SpotDetailsBottomSheetUiState())
        private set

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationTracking() {
        locationTrackingUseCase.startTracking()
        observeLocation()
    }

    fun stopLocationTracking() {
        locationTrackingUseCase.stopTracking()
    }

    fun onSpotClick(spotId: String) {
        uiState = uiState.copy(
            isModalOpen = true,
            selectedSpotId = spotId,
            selectedSpotDetails = null,
            isSpotDetailsLoading = true,
        )
    }

    fun loadSpotDetails(spotId: String) = viewModelScope.launch {
        getSpotDetailsUseCase(spotId).onSuccess { spotDetails ->
            uiState = uiState.copy(
                isSpotDetailsLoading = false,
                isModalOpen = true,
                selectedSpotDetails = spotDetails,
                selectedSpotId = spotId,
            )
        }.onFailure { error ->
            uiState = uiState.copy(
                isSpotDetailsLoading = false,
                isModalOpen = false,
                selectedSpotDetails = null,
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
        )
    }

    fun openReview() {
        uiState = uiState.copy(sheetMode = BottomSheetMode.REVIEW)
    }

    fun openDetails() {
        uiState = uiState.copy(sheetMode = BottomSheetMode.DETAILS)
    }

    private fun observeLocation() = viewModelScope.launch {
        locationTrackingUseCase.currentLocation.collect { location ->
            uiState = uiState.copy(userLocation = location)
        }
    }

}