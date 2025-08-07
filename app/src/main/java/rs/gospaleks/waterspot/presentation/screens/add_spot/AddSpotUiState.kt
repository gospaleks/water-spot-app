package rs.gospaleks.waterspot.presentation.screens.add_spot

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum

data class AddSpotUiState(
    val startLocation: LatLng? = null,
    val selectedLocation: LatLng? = null, // Goes to the database
    val allowedRadiusMeters: Double = 100.0,
    val hasCenteredCamera: Boolean = false,
    val photoUri: Uri? = null, // Goes to the database
    val type: SpotTypeEnum? = null, // Goes to the database
    val cleanliness: CleanlinessLevelEnum? = null, // Goes to the database
    val description: String? = null, // Goes to the database
)