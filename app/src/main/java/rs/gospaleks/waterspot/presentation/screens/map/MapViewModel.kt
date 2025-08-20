package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import rs.gospaleks.waterspot.domain.use_case.GetUsersWithLocationSharingUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTrackingUseCase: LocationTrackingUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase,
    private val getUsersWithLocationSharingUseCase: GetUsersWithLocationSharingUseCase,
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var hasCenteredMap = false
    private var observeJob: Job? = null

    init {
        observeUsersWithLocationSharing()

        val result = getCurrentUserUseCase()
        if (result.isSuccess) {
            uiState = uiState.copy(
                currentUserId = result.getOrNull(),
            )
        }
    }

    private fun observeUsersWithLocationSharing() {
        viewModelScope.launch {
            getUsersWithLocationSharingUseCase().collect { result ->
                result
                    .onSuccess { users ->
                        uiState = uiState.copy(usersWithLocationSharing = users)
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(error = error.message ?: "Unknown error")
                    }
            }
        }
    }

//    @OptIn(FlowPreview::class)
//    private fun observeLocation() {
//        viewModelScope.launch {
//            locationTrackingUseCase.currentLocation
//                .filterNotNull()
//                .debounce(10_000L)
//                .collectLatest { location ->
//                    Log.d("MapViewModel", "New location: $location")
//                    uiState = uiState.copy(location = location)
//                    observeSpots(location)
//                }
//        }
//    }

    // Znaci lokaciju ne uzimamo non stop vec na svakih 15 metara ili 10 sekundi ako se korisnik krece brzo
    // Ovo je dovoljno da se ne spamuje backend i da se ne trose resursi
    @OptIn(FlowPreview::class)
    private fun observeLocation() {
        viewModelScope.launch {
            locationTrackingUseCase.currentLocation
                .filterNotNull()
                .distinctUntilChanged { old, new ->
                    val distance = GeoFireUtils.getDistanceBetween(
                        GeoLocation(old.latitude, old.longitude),
                        GeoLocation(new.latitude, new.longitude)
                    )
                    distance < 15
                }
                .onEach { location ->
                    if (uiState.allSpots.isEmpty()) {
                        uiState = uiState.copy(location = location)
                        observeSpots(location)
                    }
                }
                .debounce(10_000L)
                .collectLatest { location ->
                    uiState = uiState.copy(location = location)
                    observeSpots(location)
                }
        }
    }

    private fun observeSpots(location: LatLng) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            uiState = uiState.copy(isLoadingSpots = true, error = null)
            getAllSpotsWithUserUseCase(
                location.latitude,
                location.longitude,
                uiState.radiusMeters.toDouble(),
            ).collectLatest { result ->
                result
                    .onSuccess { spots ->
                        uiState = uiState.copy(
                            allSpots = spots,
                            isLoadingSpots = false,
                            error = null
                        )
                        applyFilters()
                    }
                    .onFailure { error ->
                        uiState = uiState.copy(
                            isLoadingSpots = false,
                            error = error.message ?: "Unknown error"
                        )
                    }
            }
        }
    }

    private fun applyFilters() {
        val types = uiState.selectedTypeFilters
        val cleanliness = uiState.selectedCleanlinessFilters

        val filtered = uiState.allSpots.filter { item ->
            val spot = item.spot
            val typeOk = if (types.isEmpty()) true else types.contains(spot.type)
            val cleanlinessOk = if (cleanliness.isEmpty()) true else cleanliness.contains(spot.cleanliness)
            typeOk && cleanlinessOk
        }

        uiState = uiState.copy(filteredSpots = filtered)
    }

    // Location
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startLocationUpdates() {
        locationTrackingUseCase.startTracking()
        observeLocation()
        uiState = uiState.copy(isLocationPermissionGranted = true)
    }

    fun stopLocationUpdates() {
        locationTrackingUseCase.stopTracking()
    }

    fun shouldCenterMap(): Boolean = !hasCenteredMap

    fun setCentered() { hasCenteredMap = true }

    // Local filters management
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

    // Slider moves (no fetch yet)
    fun updateRadiusMeters(meters: Int) {
        if (meters != uiState.radiusMeters) {
            uiState = uiState.copy(radiusMeters = meters)
        }
    }

    // Called when Apply pressed in radius section
    fun applyRadiusChange() {
        uiState.location?.let { observeSpots(it) }
    }

    fun clearAllFilters() {
        uiState = uiState.copy(
            selectedTypeFilters = emptySet(),
            selectedCleanlinessFilters = emptySet(),
            radiusMeters = DEFAULT_RADIUS_METERS_MAP
        )
        applyRadiusChange()
        applyFilters()
    }
}
