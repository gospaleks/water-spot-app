package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.SpotWithUser

data class MapUiState(
    // Current user location
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,

    // List of spots
    val spots: List<SpotWithUser> = emptyList(),
    val isLoadingSpots: Boolean = false,

    // Filter options
    val filters: MapFilters = MapFilters(),

    // Error handling
    val error: String? = null,
)

data class MapFilters(
    val radius: Double = 500.0,
)