package rs.gospaleks.waterspot.presentation.screens.add_spot

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.use_case.spot.AddSpotUseCase
import rs.gospaleks.waterspot.domain.use_case.location.LocationTrackingUseCase
import rs.gospaleks.waterspot.presentation.components.UiEvent

@HiltViewModel
class AddSpotViewModel @Inject constructor(
    private val locationTrackingUseCase: LocationTrackingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val addSpotUseCase: AddSpotUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AddSpotUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        locationTrackingUseCase.startTracking()
        observeLocation()
    }

    fun stopLocationUpdates() {
        locationTrackingUseCase.stopTracking()
    }

    fun submit() {
        if (!canSubmitSpot()) {
            return
        }

        val currentUserIdResult = getCurrentUserUseCase()
        val uid = currentUserIdResult.getOrNull()

        if (uid == null) {
            uiState = uiState.copy(errorMessage = "User not authenticated")
            return
        }

        val spot = Spot(
            id = "",
            latitude = uiState.selectedLocation?.latitude ?: 0.0,
            longitude = uiState.selectedLocation?.longitude ?: 0.0,
            type = uiState.type ?: SpotTypeEnum.OTHER,
            cleanliness = uiState.cleanliness ?: CleanlinessLevelEnum.CLEAN,
            description = uiState.description,
            userId = uid,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        uiState = uiState.copy(isSubmitting = true, errorMessage = null)

        viewModelScope.launch {
           val result = addSpotUseCase(spot, uiState.photoUri!!)

            uiState = if (result.isSuccess) {
                eventFlow.emit(UiEvent.NavigateToHome)
                uiState.copy(isSubmitting = false, errorMessage = null)
            } else {
                eventFlow.emit(UiEvent.Error)
                Log.d("AddSpotViewModel", "Failed to add spot: ${result.exceptionOrNull()?.message}")
                uiState.copy(
                    isSubmitting = false,
                    errorMessage = "Failed to add spot: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }

    fun setSelectedLocation(location: LatLng) {
        uiState = uiState.copy(selectedLocation = location)
    }

    fun setPhotoUri(photoUri: Uri?) {
        uiState = uiState.copy(photoUri = photoUri)
    }

    fun setType(type: SpotTypeEnum) {
        uiState = uiState.copy(type = type)
    }

    fun setCleanliness(cleanliness: CleanlinessLevelEnum) {
        uiState = uiState.copy(cleanliness = cleanliness)
    }

    fun setDescription(description: String?) {
        uiState = uiState.copy(description = description)
    }

    fun shouldCenterCamera(): Boolean {
        return !uiState.hasCenteredCamera
    }

    fun setCameraCentered() {
        uiState = uiState.copy(hasCenteredCamera = true)
    }

    // Proverava da li moze submitovati spot
    fun canSubmitSpot(): Boolean {
        return uiState.selectedLocation != null &&
               uiState.photoUri != null &&
               uiState.type != null &&
               uiState.cleanliness != null &&
               isWithinRadius()
//               && uiState.description != null
    }

    // Proverava da li je lokacija unutar kruga
    fun isWithinRadius(): Boolean {
        val origin = uiState.startLocation ?: return false
        val target = uiState.selectedLocation ?: return false
        return distanceBetween(origin, target) <= uiState.allowedRadiusMeters
    }

    // Haversine formula to calculate distance between two LatLng points
    private fun distanceBetween(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    private fun observeLocation() = viewModelScope.launch {
        locationTrackingUseCase.currentLocation.collect { location ->
            uiState = uiState.copy(startLocation = location)
        }
    }
}