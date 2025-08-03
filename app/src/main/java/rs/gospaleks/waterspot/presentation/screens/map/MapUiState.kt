package rs.gospaleks.waterspot.presentation.screens.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings

data class MapUiState(
    var location : LatLng? = null,
    val properties: MapProperties = MapProperties(
        isMyLocationEnabled = false,
        mapType = MapType.NORMAL,
        isBuildingEnabled = false,
        isTrafficEnabled = false,
        isIndoorEnabled = false,
        mapStyleOptions = null
    ),
    val uiSettings: MapUiSettings = MapUiSettings(
        myLocationButtonEnabled = true,
        zoomControlsEnabled = false,
        compassEnabled = true,
    ),
    val isLocationPermissionGranted: Boolean = false,
    val markers: List<LatLng> = emptyList()
)
