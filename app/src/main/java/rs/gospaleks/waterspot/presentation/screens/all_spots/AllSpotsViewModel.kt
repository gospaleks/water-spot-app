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
    // Store radius in meters for finer control (supports sub-km)
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
    val error: String? = null
)

const val DEFAULT_RADIUS_METERS = 5_000

@HiltViewModel
class AllSpotsViewModel @Inject constructor(
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase,
    locationTrackingUseCase: LocationTrackingUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AllSpotsUiState())
        private set

    var textFieldState by mutableStateOf(TextFieldState())

    private var currLocation: LatLng = LatLng(0.0, 0.0)
    private var observeJob: Job? = null

    init {
        currLocation = locationTrackingUseCase.currentLocation.value ?: currLocation
        // Initial fetch with default radius (meters)
        observeSpots(radiusMeters = uiState.radiusMeters)
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

    // Slider moves (no fetch yet)
    fun updateRadiusMeters(meters: Int) {
        if (meters != uiState.radiusMeters) {
            uiState = uiState.copy(radiusMeters = meters)
        }
    }

    // Called when slider finishes or quick-chip selected
    fun applyRadiusChange() {
        observeSpots(radiusMeters = uiState.radiusMeters, forceLoading = true)
    }

    fun clearAllFilters() {
        val reset = uiState.copy(
            selectedTypeFilters = emptySet(),
            selectedCleanlinessFilters = emptySet(),
            radiusMeters = DEFAULT_RADIUS_METERS
        )
        uiState = reset
        applyFilters()
        // Refresh for default radius
        observeSpots(radiusMeters = uiState.radiusMeters, forceLoading = true)
    }

    fun observeSpots(radiusMeters: Int, forceLoading: Boolean = false) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            // Show loading only on first load (empty) or if explicitly forced (e.g., radius change)
            val shouldShowLoading = forceLoading || uiState.allSpots.isEmpty()
            uiState = uiState.copy(isLoading = shouldShowLoading)

            getAllSpotsWithUserUseCase(
                currLocation.latitude,
                currLocation.longitude,
                radiusMeters.toDouble()
            ).collect { result ->
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