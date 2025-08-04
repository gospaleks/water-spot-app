package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

data class MapUiState(
    var location : LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,
    val markers: List<LatLng> = emptyList()
)
