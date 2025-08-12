package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.SpotWithUser

data class MapUiState(
    // Current user location
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,

    // List of spots (observable)
    val spots: List<SpotWithUser> = emptyList(),
    val isLoadingSpots: Boolean = false,

    // Error handling
    val error: String? = null,
)