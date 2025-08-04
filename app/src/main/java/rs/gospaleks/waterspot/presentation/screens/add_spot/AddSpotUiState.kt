package rs.gospaleks.waterspot.presentation.screens.add_spot

import com.google.android.gms.maps.model.LatLng

data class AddSpotUiState(
    val startLocation: LatLng? = null,
    val selectedLocation: LatLng? = null,
    val allowedRadiusMeters: Double = 100.0,
    val hasCenteredCamera: Boolean = false
)