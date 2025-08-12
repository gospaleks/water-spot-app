package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase

data class SpotDetailsBottomSheetUiState(
    val isModalOpen: Boolean = false,
    val sheetMode: BottomSheetMode = BottomSheetMode.DETAILS,
    val userLocation: LatLng? = null,
    val selectedSpot: SpotWithUser? = null,
)

enum class BottomSheetMode { DETAILS, REVIEW }

@HiltViewModel
class SpotDetailsBottomSheetViewModel @Inject constructor(
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

    fun onSpotClick(spot: SpotWithUser) {
        uiState = uiState.copy(
            isModalOpen = true,
            selectedSpot = spot,
            sheetMode = BottomSheetMode.DETAILS,
        )
    }

    fun dismissBottomSheet() {
        uiState = uiState.copy(
            sheetMode = BottomSheetMode.DETAILS,
            isModalOpen = false,
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