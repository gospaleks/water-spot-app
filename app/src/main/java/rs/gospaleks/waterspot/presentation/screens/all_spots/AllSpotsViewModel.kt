package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import javax.inject.Inject

data class AllSpotsUiState(
    val isLoading: Boolean = false,
    // Full list fetched from the backend for the current radius
    val allSpots: List<SpotWithUser> = emptyList(),
    // List after applying local filters and search
    val filteredSpots: List<SpotWithUser> = emptyList(),
    // Filters
    val selectedTypeFilters: Set<SpotTypeEnum> = emptySet(),
    val selectedCleanlinessFilters: Set<CleanlinessLevelEnum> = emptySet(),
    val searchQuery: String = "",
    // Radius in kilometers (default 5 km)
    val radiusKm: Int = 5,
    val error: String? = null
)

@HiltViewModel
class AllSpotsViewModel @Inject constructor(
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase,
    private val locationTrackingUseCase: LocationTrackingUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AllSpotsUiState())
        private set

    var textFieldState by mutableStateOf(TextFieldState())

    private var currLocation: LatLng = LatLng(0.0, 0.0)
    private var observeJob: Job? = null

    init {
        currLocation = locationTrackingUseCase.currentLocation.value ?: currLocation
        // Initial fetch with default radius
        observeSpots(radiusKm = uiState.radiusKm)
    }

    // Public API to update filters/search
    fun toggleTypeFilter(type: SpotTypeEnum) {
        val newSet = uiState.selectedTypeFilters.toMutableSet().apply {
            if (contains(type)) remove(type) else add(type)
        }.toSet()
        uiState = uiState.copy(selectedTypeFilters = newSet)
        applyFilters()
    }

    fun toggleCleanlinessFilter(level: CleanlinessLevelEnum) {
        val newSet = uiState.selectedCleanlinessFilters.toMutableSet().apply {
            if (contains(level)) remove(level) else add(level)
        }.toSet()
        uiState = uiState.copy(selectedCleanlinessFilters = newSet)
        applyFilters()
    }

    fun setSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query)
        applyFilters()
    }

    // Called while the slider moves (do not re-fetch)
    fun updateRadiusKm(km: Int) {
        if (km != uiState.radiusKm) {
            uiState = uiState.copy(radiusKm = km)
        }
    }

    // Call this when user releases the slider to re-fetch data for the new radius
    fun applyRadiusChange() {
        observeSpots(radiusKm = uiState.radiusKm)
    }

    private fun observeSpots(radiusKm: Int) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val radiusMeters = radiusKm * 1000.0
            getAllSpotsWithUserUseCase(currLocation.latitude, currLocation.longitude, radiusMeters).collect { result ->
                result
                    .onSuccess { spots ->
                        uiState = uiState.copy(
                            allSpots = spots,
                            isLoading = false,
                            error = null
                        )
                        applyFilters()
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(
                            isLoading = false,
                            allSpots = emptyList(),
                            filteredSpots = emptyList(),
                            error = error.message ?: "Unknown error"
                        )
                    }
            }
        }
    }

    private fun applyFilters() {
        val query = uiState.searchQuery.trim().lowercase()
        val types = uiState.selectedTypeFilters
        val cleanliness = uiState.selectedCleanlinessFilters

        val filtered = uiState.allSpots.filter { item ->
            val spot = item.spot
            // Type filter (if set)
            val typeOk = if (types.isEmpty()) true else types.contains(spot.type)
            // Cleanliness filter (if set)
            val cleanlinessOk = if (cleanliness.isEmpty()) true else cleanliness.contains(spot.cleanliness)

            val searchOk = if (query.isEmpty()) true else run {
                val haystack = buildString {
                    append(spot.description ?: "")
                    append(' ')
                    append(spot.type.name)
                    append(' ')
                    append(spot.cleanliness.name)
                    append(' ')
                    append(item.user?.fullName ?: "")
                }.lowercase()
                haystack.contains(query)
            }

            typeOk && cleanlinessOk && searchOk
        }

        uiState = uiState.copy(filteredSpots = filtered)
    }
}