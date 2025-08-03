package rs.gospaleks.waterspot.presentation.screens.add_spot

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AddSpotScreen(
    viewModel: AddSpotViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val uiState = viewModel.uiState

    val startLocation = uiState.startLocation
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(44.0, 21.0), // default lokacija
            17f
        )
    }
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Ako ima dozvola, fetch-uj trenutnu lokaciju korisnika oko koje se prikazuje krug
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.fetchInitialLocation()
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

    // Animiraj kameru kada se startLocation ucita
    LaunchedEffect(startLocation) {
        if (startLocation != null) {
            val radiusInKm = uiState.allowedRadiusMeters / 1000.0
            val zoomLevel = when {
                radiusInKm <= 0.1 -> 17f  // 100m ili manje
                radiusInKm <= 0.25 -> 16f // 250m ili manje
                radiusInKm <= 0.5 -> 15f  // 500m ili manje
                radiusInKm <= 1.0 -> 14f  // 1km ili manje
                radiusInKm <= 2.0 -> 13f  // 2km ili manje
                else -> 12f               // vise od 2km
            }
            val targetPosition = CameraPosition.fromLatLngZoom(startLocation, zoomLevel)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(targetPosition),
                durationMs = 1000
            )
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
                if (startLocation != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(zoomControlsEnabled = false)
                        ) {
                            Circle(
                                center = startLocation,
                                fillColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                strokeColor = Color(0xFF4CAF50),
                                strokeWidth = 4f,
                                radius = uiState.allowedRadiusMeters
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Location picker",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center)
                                .offset(y = (-24).dp)
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
                    }
                )
            }

            // Bottom spacer to keep button away from screen edge
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))
        }

    }
}