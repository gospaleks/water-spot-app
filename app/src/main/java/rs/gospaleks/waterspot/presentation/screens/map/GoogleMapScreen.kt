package rs.gospaleks.waterspot.presentation.screens.map

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import rs.gospaleks.waterspot.R

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GoogleMapScreen(
    innerPadding: PaddingValues,
    viewModel: MapViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val cameraPositionState = rememberCameraPositionState()

    // Trazenje permisije i pokretanje lokacijskog servisa
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            viewModel.startLocationUpdates()
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        if (locationPermissionState.status.isGranted) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = uiState.properties,
                uiSettings = uiState.uiSettings,
            ) {

            }
        } else {
            PermissionDeniedPlaceholder()
        }

    }
}

@Composable
fun PermissionDeniedPlaceholder() {
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOff,
            contentDescription = "Location disabled",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.permission_denied_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}