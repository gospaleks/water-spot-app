package rs.gospaleks.waterspot.presentation.screens.add_spot

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Circle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddSpotScreen(
    viewModel: AddSpotViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val uiState = viewModel.uiState

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(44.0, 21.0), // default lokacija
            17f
        )
    }
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )
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
    // Update selectedLocation kad korisnik pomera mapu
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) {
            viewModel.setSelectedLocation(cameraPositionState.position.target)
        }
    }

    // Animiraj kameru kada se startLocation učita
    LaunchedEffect(uiState.startLocation) {
        if (uiState.startLocation != null && viewModel.shouldCenterCamera()) {
            val radiusInKm = uiState.allowedRadiusMeters / 1000.0
            val zoomLevel = when {
                radiusInKm <= 0.1 -> 17f  // 100m ili manje
                radiusInKm <= 0.25 -> 16f // 250m ili manje
                radiusInKm <= 0.5 -> 15f  // 500m ili manje
                radiusInKm <= 1.0 -> 14f  // 1km ili manje
                radiusInKm <= 2.0 -> 13f  // 2km ili manje
                else -> 12f               // vise od 2km
            }
            val targetPosition = CameraPosition.fromLatLngZoom(uiState.startLocation, zoomLevel)
            cameraPositionState.move(
                update = CameraUpdateFactory.newCameraPosition(targetPosition)
            )
            viewModel.setCameraCentered()
        }
    }

    // Zaustavljanje azuriranja lokacije kada se ekran zatvori
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationUpdates()
        }
    }

    Scaffold (
        topBar = {
            BasicTopAppBar(
                title = stringResource(id = R.string.add_spot_title),
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
        ) {
            Text(
                text = uiState.startLocation?.latitude.toString()
            )
            // Title at the top with vertical padding
            Text(
                text = stringResource(R.string.add_spot_instructions),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_extra_large))
            )

            // Map takes all available space between title and button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (locationPermissionState.status.isGranted) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                mapStyleOptions = mapStyleJson,
                                isMyLocationEnabled = locationPermissionState.status.isGranted
                            ),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false, compassEnabled = false)
                        ) {
                            if (uiState.startLocation != null) {
                                Circle(
                                    center = uiState.startLocation,
                                    fillColor = Color(0xFF81C784).copy(alpha = 0.4f),
                                    strokeColor = Color(0xFF81C784),
                                    strokeWidth = 4f,
                                    radius = uiState.allowedRadiusMeters
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location picker",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(58.dp)
                                .align(Alignment.Center)
                                .offset(y = (-29).dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.permission_denied_message),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Spacer between map and button
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))

            // Button at the bottom
            Button(
                onClick = onNextClick,
                enabled = viewModel.isWithinRadius(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (viewModel.isWithinRadius()) {
                        stringResource(R.string.add_spot_confirmation)
                    } else {
                        stringResource(R.string.add_spot_location_error)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Bottom spacer to keep button away from screen edge
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))
        }

    }
}