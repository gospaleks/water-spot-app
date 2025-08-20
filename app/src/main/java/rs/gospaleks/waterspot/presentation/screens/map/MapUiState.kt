package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.model.User

data class MapUiState(
    // Current user location
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,
    val currentUserId: String? = null,

    // List of users with location sharing enabled
    val usersWithLocationSharing: List<User> = emptyList(),

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