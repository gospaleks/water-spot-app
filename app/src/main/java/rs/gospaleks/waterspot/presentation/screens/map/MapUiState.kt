package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.model.User

const val DEFAULT_RADIUS_METERS_MAP = 20_000

data class MapUiState(
    // Current user location
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,
    val currentUserId: String? = null,

    // List of users with location sharing enabled
    val usersWithLocationSharing: List<User> = emptyList(),

    // Full list fetched from the backend for the applied radius
    val allSpots: List<SpotWithUser> = emptyList(),
    // Locally filtered list for display
    val filteredSpots: List<SpotWithUser> = emptyList(),
    val isLoadingSpots: Boolean = false,

    // Local filters (do not trigger backend calls)
    val selectedTypeFilters: Set<SpotTypeEnum> = emptySet(),
    val selectedCleanlinessFilters: Set<CleanlinessLevelEnum> = emptySet(),

    // Radius UI state (meters) (trigger backend calls)
    val radiusMeters: Int = DEFAULT_RADIUS_METERS_MAP,

    // Error handling
    val error: String? = null,
)