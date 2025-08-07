package rs.gospaleks.waterspot.presentation.screens.add_spot

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@HiltViewModel
class AddSpotViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : ViewModel() {
    var uiState by mutableStateOf(AddSpotUiState())
        private set

    fun fetchInitialLocation() = viewModelScope.launch {
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                uiState = uiState.copy(startLocation = LatLng(it.latitude, it.longitude))
            }
        } catch (e: SecurityException) {
            // Handle missing permission
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setSelectedLocation(location: LatLng) {
        uiState = uiState.copy(selectedLocation = location)
    }

    fun setPhotoUri(photoUri: Uri?) {
        uiState = uiState.copy(photoUri = photoUri)
    }

    fun shouldCenterCamera(): Boolean {
        return !uiState.hasCenteredCamera
    }

    fun setCameraCentered() {
        uiState = uiState.copy(hasCenteredCamera = true)
    }

    // Proverava da li je lokacija unutar kruga
    fun isWithinRadius(): Boolean {
        val origin = uiState.startLocation ?: return false
        val target = uiState.selectedLocation ?: return false
        return distanceBetween(origin, target) <= uiState.allowedRadiusMeters
    }

    // Haversine formula to calculate distance between two LatLng points
    private fun distanceBetween(start: LatLng, end: LatLng): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(end.latitude - start.latitude)
        val dLon = Math.toRadians(end.longitude - start.longitude)
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)

        val a = sin(dLat / 2).pow(2) + sin(dLon / 2).pow(2) * cos(lat1) * cos(lat2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}