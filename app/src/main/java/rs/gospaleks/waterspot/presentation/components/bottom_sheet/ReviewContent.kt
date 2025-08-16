package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Surface
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
    onSubmitReview: (String, Float) -> Unit,
    isInZone: Boolean,
    distanceMeters: Float?
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


    LazyColumn (
        modifier = Modifier.padding(horizontal = 24.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.review_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Map View
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
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
                            fillColor = Color(0xFF81C784).copy(alpha = 0.35f),
                            strokeColor = Color(0xFF81C784),
                            strokeWidth = 3f,
                            radius = 50.0,
                        )
                    }
                }
            }
        }

        if (!isInZone) {
            item {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.RateReview,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${stringResource(R.string.review_dialog_move_closer)} ${distanceMeters?.toInt() ?: "?"} m)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Rating
        item {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = stringResource(R.string.review_dialog_rating_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val starCount = 5
                        for (i in 1..starCount) {
                            val selected = i <= starRating.toInt()
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(enabled = isInZone) { starRating = i.toFloat() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = "Star $i",
                                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
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
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
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