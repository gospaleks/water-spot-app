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

    // Bottom Sheet
    val sheetMode: BottomSheetMode = BottomSheetMode.DETAILS,

    val isModalOpen: Boolean = false,
    val isSpotDetailsLoading: Boolean = false,
    val selectedSpotDetails: SpotDetails? = null,
    val selectedSpotId: String? = null,

    // Error handling
    val error: String? = null,
)

enum class BottomSheetMode { DETAILS, REVIEW }
