package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
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
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.AppTheme
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.screens.map.components.CustomFABs
import rs.gospaleks.waterspot.presentation.screens.map.components.MapTopAppBar
import rs.gospaleks.waterspot.presentation.screens.map.components.PermissionDeniedPlaceholder
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheet
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheetViewModel
import rs.gospaleks.waterspot.presentation.screens.profile.ThemeViewModel

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapScreen(
    navigateToAddSpotScreen: () -> Unit,
    outerPadding: PaddingValues,
    viewModel: MapViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val bottomSheetViewModel: SpotDetailsBottomSheetViewModel = hiltViewModel()

    val uiState = viewModel.uiState

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current

    // Uzmi temu iz theme viewmodel-a i na osnovu nje promeni map style
    val myTheme by themeViewModel.appTheme.collectAsState(initial = AppTheme.SYSTEM)

    val isDark = when (myTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }

    val mapStyleJson = if (isDark) {
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
                CameraUpdateFactory.newLatLngZoom(uiState.location!!, 18f)
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
            MapTopAppBar(
                currentFilters = uiState.filters,
                onFilterApply = { filters ->
                    viewModel.updateFilters(filters)
                }
            )
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
                    uiState.spots.forEach { item ->
                        Marker(
                            state = MarkerState(
                                position = LatLng(item.spot.latitude, item.spot.longitude),
                            ),
                            title = item.spot.type.toDisplayName(),
                            snippet = item.spot.description ?: "",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                            onClick = {
                                bottomSheetViewModel.onSpotClick(item)
                                true
                            }
                        )
                    }
                }

                SpotDetailsBottomSheet(viewModel = bottomSheetViewModel)

            } else {
                PermissionDeniedPlaceholder()
            }
        }
    }
}