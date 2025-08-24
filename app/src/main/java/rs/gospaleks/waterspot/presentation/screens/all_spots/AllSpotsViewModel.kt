package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

// Date filter presets for updatedAt field
enum class DateFilterPreset { ANY, OLDER_WEEK, OLDER_MONTH, OLDER_6_MONTHS, OLDER_YEAR, CUSTOM }

data class AllSpotsUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    // Full list fetched from the backend for the current radius
    val allSpots: List<SpotWithUser> = emptyList(),
    // Filters
    val selectedTypeFilters: Set<SpotTypeEnum> = emptySet(),
    val selectedCleanlinessFilters: Set<CleanlinessLevelEnum> = emptySet(),
    val searchQuery: String = "",
    // Store radius in meters for finer control (supports sub-km)
    val radiusMeters: Int = DEFAULT_RADIUS_METERS,
    // Date filter
    val dateFilterPreset: DateFilterPreset = DateFilterPreset.ANY,
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
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

    val filteredSpots = derivedStateOf {
        val query = uiState.searchQuery.trim().lowercase()
        val types = uiState.selectedTypeFilters
        val cleanliness = uiState.selectedCleanlinessFilters
        val preset = uiState.dateFilterPreset
        val customStart = uiState.customStartDateMillis
        val customEnd = uiState.customEndDateMillis

        val now = System.currentTimeMillis()
        val olderThanMillis = when (preset) {
            DateFilterPreset.OLDER_WEEK -> 7L * 24 * 60 * 60 * 1000
            DateFilterPreset.OLDER_MONTH -> 30L * 24 * 60 * 60 * 1000
            DateFilterPreset.OLDER_6_MONTHS -> 182L * 24 * 60 * 60 * 1000
            DateFilterPreset.OLDER_YEAR -> 365L * 24 * 60 * 60 * 1000
            else -> null
        }

        uiState.allSpots.filter { item ->
            val spot = item.spot
            val typeOk = types.isEmpty() || types.contains(spot.type)
            val cleanlinessOk = cleanliness.isEmpty() || cleanliness.contains(spot.cleanliness)

            val searchOk = if (query.isEmpty()) true else {
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

            val updatedAt = spot.updatedAt
            val dateOk = when (preset) {
                DateFilterPreset.ANY -> true
                DateFilterPreset.CUSTOM -> {
                    if (updatedAt == null) false else {
                        val lowerOk = customStart?.let { updatedAt >= it } ?: true
                        val upperOk = customEnd?.let { updatedAt <= it } ?: true
                        lowerOk && upperOk
                    }
                }
                else -> {
                    if (updatedAt == null) false else {
                        val threshold = now - (olderThanMillis ?: 0L)
                        updatedAt <= threshold
                    }
                }
            }

            typeOk && cleanlinessOk && searchOk && dateOk
        }
    }


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
    }

    fun toggleCleanlinessFilter(level: CleanlinessLevelEnum) {
        val newSet = uiState.selectedCleanlinessFilters.toMutableSet().apply {
            if (contains(level)) remove(level) else add(level)
        }.toSet()
        uiState = uiState.copy(selectedCleanlinessFilters = newSet)
    }

    fun setSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    // Date filter: preset selection
    fun setDatePreset(preset: DateFilterPreset) {
        uiState = uiState.copy(
            dateFilterPreset = preset,
            // Clear custom range when switching away from CUSTOM
            customStartDateMillis = if (preset == DateFilterPreset.CUSTOM) uiState.customStartDateMillis else null,
            customEndDateMillis = if (preset == DateFilterPreset.CUSTOM) uiState.customEndDateMillis else null,
        )
    }

    // Date filter: set custom range (also sets preset to CUSTOM)
    fun setCustomDateRange(startMillis: Long?, endMillis: Long?) {
        uiState = uiState.copy(
            dateFilterPreset = DateFilterPreset.CUSTOM,
            customStartDateMillis = startMillis,
            customEndDateMillis = endMillis
        )
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
            radiusMeters = DEFAULT_RADIUS_METERS,
            dateFilterPreset = DateFilterPreset.ANY,
            customStartDateMillis = null,
            customEndDateMillis = null
        )
        uiState = reset
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
                            isRefreshing = false,
                            error = null,
                        )
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(
                            isLoading = false,
                            isRefreshing = false,
                            allSpots = emptyList(),
                            error = error.message ?: "Unknown error"
                        )
                    }
            }
        }
    }

    fun refresh() {
        // Re-fetch spots with current radius and apply filters that were set
        uiState = uiState.copy(isRefreshing = true)
        observeSpots(radiusMeters = uiState.radiusMeters, forceLoading = true)
    }
}