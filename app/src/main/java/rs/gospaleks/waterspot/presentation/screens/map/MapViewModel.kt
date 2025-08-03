package rs.gospaleks.waterspot.presentation.screens.map

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MapViewModel() : ViewModel() {
    var uiState by mutableStateOf(MapUiState())
        private set
}