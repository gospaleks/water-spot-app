package rs.gospaleks.waterspot.presentation.screens.map.components.bottom_sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import rs.gospaleks.waterspot.domain.model.SpotDetails
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.presentation.components.CleanlinessChip
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.screens.map.BottomSheetMode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailsBottomSheet(
    sheetMode: BottomSheetMode,
    userLocation: LatLng? = null,
    spotDetails: SpotDetails? = null,
    mapStyleJson: MapStyleOptions,
    isLoading: Boolean,
    selectedSpotId: String? = null,
    onDismiss: () -> Unit, // gasi ceo sheet
    onReviewClick: () -> Unit, // otvara review mode
    onCloseReview: () -> Unit, // zatvara review mode
    onNavigateClick: () -> Unit,
    onLoadSpotDetails: (String) -> Unit = {},
    onUserProfileClick: () -> Unit = {}
) {
    LaunchedEffect(selectedSpotId) {
        selectedSpotId?.let { spotId ->
            // Mala pauza da se animacija otvaranja završi
            delay(200)
            onLoadSpotDetails(spotId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        AnimatedContent(targetState = sheetMode) { mode ->
            when (mode) {
                BottomSheetMode.DETAILS -> {
                    if (isLoading || spotDetails == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(0.5f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        SpotDetailsContent(
                            spotDetails = spotDetails,
                            onNavigateClick = onNavigateClick,
                            onReviewClick = onReviewClick,
                            onUserProfileClick = onUserProfileClick
                        )
                    }
                }

                BottomSheetMode.REVIEW -> {
                    ReviewContent(
                        mapStyleJson = mapStyleJson,
                        spotDetails = spotDetails ?: return@AnimatedContent,
                        userLocation = userLocation,
                        onBack = onCloseReview,
                        onSubmitReview = { /* TODO: Call view model to send review */}
                    )
                }
            }

        }
    }
}

@Composable
fun ReviewContent(
    mapStyleJson: MapStyleOptions,
    spotDetails: SpotDetails,
    userLocation: LatLng?,
    onBack: () -> Unit,
    onSubmitReview: (String) -> Unit
) {
    val spotLatLng = LatLng(spotDetails.spot.latitude, spotDetails.spot.longitude)

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userLocation, spotLatLng) {
        if (userLocation != null) {
            val bounds = LatLngBounds.builder()
                .include(userLocation)
                .include(spotLatLng)
                .build()

            // pomeranje kamere sa paddingom da ne budu marker-i skroz na ivici
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngBounds(bounds, 200)
            )
        } else {
            // fallback ako nema userLocation
            cameraPositionState.position = CameraPosition.fromLatLngZoom(spotLatLng, 17f)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Header sa "Back" dugmetom
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Spacer(Modifier.width(8.dp))
            Text("Review", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInteropFilter { false },
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

        Spacer(Modifier.height(12.dp))

        // Distance & enable/disable logic
        val distanceMeters = remember(userLocation, spotLatLng) {
            userLocation?.let {
                val result = FloatArray(1)
                android.location.Location.distanceBetween(
                    it.latitude, it.longitude,
                    spotLatLng.latitude, spotLatLng.longitude,
                    result
                )
                result[0]
            }
        }
        val isInZone = distanceMeters?.let { it <= 50f } ?: false

        Text(
            text = if (isInZone) "You are inside the 50m zone — you can leave a review"
            else "Move closer to leave a review (distance: ${distanceMeters?.toInt() ?: "?"} m)",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isInZone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        // Comment input (disabled if not in zone)
        var comment by remember { mutableStateOf("") }
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            enabled = isInZone,
            placeholder = { Text("Write your review...") },
            modifier = Modifier.fillMaxWidth().height(120.dp)
        )

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                onSubmitReview(comment)
                comment = ""
            },
            enabled = isInZone && comment.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RateReview, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Submit review")
        }
    }
}


@Composable
fun SpotDetailsContent(
    spotDetails: SpotDetails,
    onReviewClick: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onUserProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 24.dp)
    ) {
        // Photo
        spotDetails.spot.photoUrl?.let { photoUrl ->
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Header with type and date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = spotDetails.spot.type.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = spotDetails.spot.type.toDisplayName(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Cleanliness chip
            CleanlinessChip(
                cleanliness = spotDetails.spot.cleanliness,
                modifier = Modifier.wrapContentWidth()
            )
        }

        // Description
        spotDetails.spot.description?.let { description ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = description,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Clickable user info
        spotDetails.user?.let { user ->
            PostedByCard(user, onUserProfileClick)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        ActionsButtons(
            onNavigateClick = onNavigateClick,
            onReviewClick = onReviewClick
        )
    }
}