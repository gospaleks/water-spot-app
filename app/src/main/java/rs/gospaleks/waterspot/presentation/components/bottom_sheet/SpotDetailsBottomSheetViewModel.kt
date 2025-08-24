package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.MutableSharedFlow
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase
import rs.gospaleks.waterspot.domain.model.Review
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.use_case.AddReviewUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import rs.gospaleks.waterspot.presentation.components.UiEvent
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.use_case.AddAditionalPhotoToSpotUseCase
import rs.gospaleks.waterspot.domain.use_case.GetAllReviewsForSpotUseCase
import rs.gospaleks.waterspot.domain.use_case.GetSpotByIdUseCase
import rs.gospaleks.waterspot.domain.use_case.IsSpotVisitedUseCase
import rs.gospaleks.waterspot.domain.use_case.MarkSpotAsVisitedUseCase

data class SpotDetailsBottomSheetUiState(
    val isModalOpen: Boolean = false,
    val sheetMode: BottomSheetMode = BottomSheetMode.DETAILS,
    val userLocation: LatLng? = null,
    val selectedSpot: SpotWithUser? = null,
    val reviews: List<ReviewWithUser> = emptyList(),
    val isLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val isSpotVisited: Boolean = false,
)

enum class BottomSheetMode { DETAILS, REVIEW }

@HiltViewModel
class SpotDetailsBottomSheetViewModel @Inject constructor(
    private val locationTrackingUseCase: LocationTrackingUseCase,
    private val addReviewUseCase: AddReviewUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllReviewsForSpotUseCase: GetAllReviewsForSpotUseCase,
    private val addAditionalPhotoToSpotUseCase: AddAditionalPhotoToSpotUseCase,
    private val getSpotByIdUseCase: GetSpotByIdUseCase,
    private val markSpotAsVisitedUseCase: MarkSpotAsVisitedUseCase,
    private val isSpotVisitedUseCase: IsSpotVisitedUseCase
) : ViewModel() {

    var uiState by mutableStateOf(SpotDetailsBottomSheetUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    private fun observeReviews() = viewModelScope.launch {
        Log.d("SpotDetailsViewModel", "Starting to observe reviews")
        uiState = uiState.copy(isLoading = true)

        val spotId = uiState.selectedSpot?.spot?.id ?: return@launch

        getAllReviewsForSpotUseCase(spotId).collect { result ->
            result
                .onSuccess { reviews ->
                    uiState = uiState.copy(
                        reviews = reviews,
                        isLoading = false,
                    )

                    Log.d("SpotDetailsViewModel", "Reviews loaded: ${reviews.size} for spot $spotId")
                }
                .onFailure { error ->
                    uiState = uiState.copy(isLoading = false)
                }
        }
    }

    fun submitReview(reviewText: String, rating: Float) {
        if (uiState.selectedSpot == null) {
            return
        }

        val currentUserIdResult = getCurrentUserUseCase()
        val uid = currentUserIdResult.getOrNull()

        if (uid == null) {
            return
        }

        val review = Review(
            comment = reviewText,
            rating = rating.toInt(),
            userId = uid,
        )

        viewModelScope.launch {
            val result = addReviewUseCase(uiState.selectedSpot!!.spot.id, review)

            if (result.isSuccess) {
                openDetails()
                eventFlow.emit(UiEvent.ShowToast("Submitted successfully!"))

                // Refresh spot data to show new average and cleanliness level if changed
                refreshSpotData(uiState.selectedSpot!!.spot.id)
            } else {
                eventFlow.emit(UiEvent.ShowToast(result.exceptionOrNull()?.message ?: "Failed to submit review"))
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun addAdditionalPhotoToSpot(photoUri: Uri) {
        if (uiState.selectedSpot == null) {
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isUploadingPhoto = true)
            val result = addAditionalPhotoToSpotUseCase(uiState.selectedSpot!!.spot.id, photoUri)

            if (result.isSuccess) {
                eventFlow.emit(UiEvent.ShowToast("Photo added successfully!"))
                uiState = uiState.copy(isUploadingPhoto = false)

                // Refresh spot data to show the new photo
                refreshSpotData(uiState.selectedSpot!!.spot.id)
            } else {
                eventFlow.emit(UiEvent.ShowToast(result.exceptionOrNull()?.message ?: "Failed to add photo"))
                result.exceptionOrNull()?.printStackTrace()
                uiState = uiState.copy(isUploadingPhoto = false)
            }
        }
    }

    fun markSpotAsVisited() {
        val spotId = uiState.selectedSpot?.spot?.id ?: return

        viewModelScope.launch {
            val result = markSpotAsVisitedUseCase(spotId)

            if (result.isSuccess) {
                eventFlow.emit(UiEvent.ShowToast("Spot marked as visited!"))
            } else {
                eventFlow.emit(UiEvent.ShowToast(result.exceptionOrNull()?.message ?: "Failed to mark spot as visited"))
                result.exceptionOrNull()?.printStackTrace()
            }
        }
    }

    fun observeIsSpotVisited(spotId: String) {
        viewModelScope.launch {
            isSpotVisitedUseCase(spotId).collect { result ->
                result
                    .onSuccess { isVisited ->
                        uiState = uiState.copy(isSpotVisited = isVisited)
                    }
                    .onFailure { error ->
                        eventFlow.emit(UiEvent.ShowToast("Failed to check if spot is visited"))
                    }
            }
        }
    }

    private suspend fun refreshSpotData(spotId: String) {
        val spotResult = getSpotByIdUseCase(spotId)
        if (spotResult.isSuccess) {
            val updatedSpot = spotResult.getOrNull()
            if (updatedSpot == null) {
                eventFlow.emit(UiEvent.ShowToast("Failed to refresh spot data"))
            }

            uiState = uiState.copy(
                selectedSpot = SpotWithUser(
                    spot = updatedSpot!!,
                    user = uiState.selectedSpot?.user
                )
            )
        } else {
            eventFlow.emit(UiEvent.ShowToast("Failed to refresh spot data"))
        }
    }

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

        observeReviews()
        observeIsSpotVisited(spot.spot.id)
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