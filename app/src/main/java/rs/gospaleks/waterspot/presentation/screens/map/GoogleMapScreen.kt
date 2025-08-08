package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.PairSerializer
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.screens.map.components.CustomFABs
import rs.gospaleks.waterspot.presentation.screens.map.components.MapTopAppBar
import rs.gospaleks.waterspot.presentation.screens.map.components.PermissionDeniedPlaceholder
import rs.gospaleks.waterspot.presentation.screens.map.components.SpotDetailsBottomSheet

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapScreen(
    navigateToAddSpotScreen: () -> Unit,
    outerPadding: PaddingValues,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val mapStyleJson = if (isDarkTheme) {
        MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_dark)
    } else {
        MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_light)
    }

    // Trazenje permisije i pokretanje lokacijskog servisa
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            // Dodatna sigurnost da je permisija stvarno data
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                viewModel.startLocationUpdates()
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    // Centriraj kameru na trenutnu lokaciju samo pri ulasku u aplikaciju
    // Kasnije kroz kretanje medju ekrane, view model cuva state
    LaunchedEffect(uiState.location) {
        if (uiState.location != null && viewModel.shouldCenterMap()) {
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(uiState.location!!, 16f)
            )
            viewModel.setCentered()
        }
    }

    // Zaustavljanje azuriranja lokacije kada se ekran zatvori
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
        }
    }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            MapTopAppBar()
        },
        floatingActionButton = {
            CustomFABs(
                outerPadding = outerPadding,
                cameraReset = {
                    uiState.location?.let { location ->
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(location, 16f)
                            )
                        }
                    }
                },
                navigateToAddSpotScreen = navigateToAddSpotScreen,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            if (locationPermissionState.status.isGranted) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapStyleOptions = mapStyleJson,
                        isMyLocationEnabled = uiState.isLocationPermissionGranted
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        compassEnabled = false,
                    )
                ) {
                    uiState.spots.forEach { spot ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(spot.latitude, spot.longitude),
                            ),
                            title = spot.type.toDisplayName(),
                            snippet = spot.description ?: "",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                            onClick = {
                                viewModel.onMarkerClick(spot.id)
                                true
                            }
                        )
                    }
                }

                if (uiState.isModalOpen) {
                    SpotDetailsBottomSheet (
                        spotDetails = uiState.selectedSpotDetails,
                        isLoading = uiState.isSpotDetailsLoading,
                        selectedSpotId = uiState.selectedSpotId,
                        onDismiss = { viewModel.dismissBottomSheet() },
                        onReportClick = { /* ... */ },
                        onNavigateClick = { /* ... */ },
                        onLoadSpotDetails = { spotId -> viewModel.loadSpotDetails(spotId) }
                    )
                }

            } else {
                PermissionDeniedPlaceholder()
            }
        }
    }
}