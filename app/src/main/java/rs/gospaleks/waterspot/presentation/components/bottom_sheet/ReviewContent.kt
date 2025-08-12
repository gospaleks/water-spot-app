package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.R

@Composable
fun ReviewContent(
    mapStyleJson: MapStyleOptions,
    data: SpotWithUser,
    userLocation: LatLng?,
    onBack: () -> Unit,
    onSubmitReview: (String, Float) -> Unit
) {
    val spotLatLng = LatLng(data.spot.latitude, data.spot.longitude)
    val cameraPositionState = rememberCameraPositionState()

    var comment by remember { mutableStateOf("") }
    var starRating by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(userLocation, spotLatLng) {
        if (userLocation != null) {
            val bounds = LatLngBounds.builder()
                .include(userLocation)
                .include(spotLatLng)
                .build()
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 200)
            )
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(spotLatLng, 17f)
        }
    }

    val distanceMeters = remember(userLocation, spotLatLng) {
        userLocation?.let {
            val result = FloatArray(1)
            Location.distanceBetween(
                it.latitude, it.longitude,
                spotLatLng.latitude, spotLatLng.longitude,
                result
            )
            result[0]
        }
    }
    val isInZone = distanceMeters?.let { it <= 50f } ?: false

    LazyColumn (
        modifier = Modifier.padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.review_dialog_title), style = MaterialTheme.typography.titleLarge)
            }
        }

        // Map View
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        mapStyleOptions = mapStyleJson,
                        isMyLocationEnabled = userLocation != null
                    ),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false,
                        compassEnabled = false,
                    )
                ) {
                    val spotMarkerState = remember { MarkerState(position = spotLatLng) }
                    Marker(
                        state = spotMarkerState,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )
                    Circle(
                        center = spotLatLng,
                        fillColor = Color(0xFF81C784).copy(alpha = 0.4f),
                        strokeColor = Color(0xFF81C784),
                        strokeWidth = 4f,
                        radius = 50.0,
                    )
                }
            }
        }

        if (!isInZone) {
            item {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${stringResource(R.string.review_dialog_move_closer)} ${distanceMeters?.toInt() ?: "?"} m)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Rating
        item {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val starCount = 5
                val spacing = 4.dp

                for (i in 1..starCount) {
                    val selected = i <= starRating.toInt()
                    Icon(
                        imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = "Star $i",
                        tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .weight(1f)
                            .aspectRatio(1f)
                            .clickable(enabled = isInZone) {
                                starRating = i.toFloat()
                            }
                    )
                    if (i != starCount) {
                        Spacer(Modifier.width(spacing))
                    }
                }
            }
        }

        // Comment input
        item {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                label = { Text(text = stringResource(R.string.review_dialog_comment_label)) },
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text(text = stringResource(R.string.review_dialog_comment_placeholder)) },
                enabled = isInZone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }

        // Submit button
        item {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSubmitReview(comment, starRating)
                    comment = ""
                    starRating = 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                enabled = isInZone && comment.isNotBlank() && starRating > 0,

            ) {
                Icon(Icons.Default.RateReview, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(R.string.review_dialog_submit_button))
            }
        }
    }
}