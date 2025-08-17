package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import rs.gospaleks.waterspot.domain.use_case.LocationTrackingUseCase
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationTrackingUseCase: LocationTrackingUseCase,
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase
) : ViewModel() {

    var uiState by mutableStateOf(MapUiState())
        private set

    private var hasCenteredMap = false

    // Znaci koja je ideja:
    // 1. Prvo se pokrene location tracking iz UI-a i onda on uzme trenutnu lokaciju i salje je use case-u da mu vrati sve spotove u okolini
    // 2. Use case uzima trenutnu lokaciju i vraca sve spotove
    // 3. Kada se lokacija promeni (barem 15m od prethodne), use case salje novu lokaciju i vraca sve spotove u okolini
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
                    // Ako je ovo prvi put, odmah pokreni
                    if (uiState.spots.isEmpty()) {
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
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoadingSpots = true,
                error = null
            )
            getAllSpotsWithUserUseCase(
                location.latitude,
                location.longitude,
                uiState.filters.radius
            ).collectLatest { result ->
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

    fun updateFilters(newFilters: MapFilters) {
        val oldRadius = uiState.filters.radius
        uiState = uiState.copy(filters = newFilters)
        if (newFilters.radius != oldRadius) {
            uiState.location?.let { location ->
                observeSpots(location)
            }
        }
    }
}
