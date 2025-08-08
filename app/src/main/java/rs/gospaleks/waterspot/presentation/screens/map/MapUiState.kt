package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.Spot
import rs.gospaleks.waterspot.domain.model.SpotDetails

data class MapUiState(
    // Current user location
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,

    // List of spots (observable)
    val spots: List<Spot> = emptyList(),
    val isLoadingSpots: Boolean = false,

    // UI state for the bottom sheet
    val isModalOpen: Boolean = false,
    val isSpotDetailsLoading: Boolean = false,
    val selectedSpotDetails: SpotDetails? = null,

    // Error handling
    val error: String? = null,
)
