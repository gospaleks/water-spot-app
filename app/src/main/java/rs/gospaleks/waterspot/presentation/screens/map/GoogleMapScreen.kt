package rs.gospaleks.waterspot.presentation.screens.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun GoogleMapScreen(
    innerPadding: PaddingValues,
    viewModel: MapViewModel = viewModel()
) {
    val cameraPositionState = rememberCameraPositionState()
    val uiState = viewModel.uiState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = uiState.properties,
            uiSettings = uiState.uiSettings,
        ) {
            
        }
    }
}