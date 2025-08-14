package rs.gospaleks.waterspot.presentation.screens.all_spots

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import javax.inject.Inject

data class AllSpotsUiState(
    val isLoading: Boolean = false,
    val spotsWithUser: List<SpotWithUser> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AllSpotsViewModel @Inject constructor(
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase,
    private val locationTrackingUseCase: LocationTrackingUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AllSpotsUiState())
        private set

    private var currLocation: LatLng = LatLng(0.0, 0.0)

    init {
        currLocation = locationTrackingUseCase.currentLocation.value ?: currLocation

        observeSpots()
    }

    private fun observeSpots() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        getAllSpotsWithUserUseCase(currLocation.latitude, currLocation.longitude).collect { result ->
            result
                .onSuccess { spots ->
                    uiState = uiState.copy(
                        spotsWithUser = spots,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        spotsWithUser = emptyList(),
                        error = error.message ?: "Unknown error"
                    )
                }
        }
    }
}