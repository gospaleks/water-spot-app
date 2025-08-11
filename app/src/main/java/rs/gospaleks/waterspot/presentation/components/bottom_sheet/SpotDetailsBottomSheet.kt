package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
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
import rs.gospaleks.waterspot.presentation.components.CleanlinessChip
import rs.gospaleks.waterspot.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SpotDetailsBottomSheet(
    viewModel: SpotDetailsBottomSheetViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()

    val mapStyleJson = remember(isDarkTheme) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (isDarkTheme) R.raw.map_dark else R.raw.map_light
        )
    }

    // Permission and location tracking
    LaunchedEffect(locationPermission.status) {
        if (locationPermission.status.isGranted) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                viewModel.startLocationTracking()
            }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopLocationTracking() }
    }

    // Load spot details on open
    LaunchedEffect(uiState.selectedSpotId) {
        delay(200) // wait for animation
        uiState.selectedSpotId?.let {
            if (uiState.selectedSpotDetails == null) {
                viewModel.loadSpotDetails(it)
            }
        }
    }

    if (uiState.isModalOpen) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissBottomSheet) {
            AnimatedContent(targetState = uiState.sheetMode) { mode ->
                when (mode) {
                    BottomSheetMode.DETAILS -> SpotDetailsBody(uiState, viewModel, context)
                    BottomSheetMode.REVIEW -> {
                        uiState.selectedSpotDetails?.let {
                            ReviewContent(
                                spotDetails = it,
                                userLocation = uiState.userLocation,
                                mapStyleJson = mapStyleJson,
                                onBack = viewModel::openDetails,
                                onSubmitReview = { /* handle review */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotDetailsBody(
    uiState: SpotDetailsBottomSheetUiState,
    viewModel: SpotDetailsBottomSheetViewModel,
    context: Context,
) {
    if (uiState.isSpotDetailsLoading || uiState.selectedSpotDetails == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        SpotDetailsContent(
            spotDetails = uiState.selectedSpotDetails,
            onNavigateClick = {
                val data = uiState.selectedSpotDetails
                val uri =
                    "google.navigation:q=${data.spot.latitude},${data.spot.longitude}&mode=w".toUri()
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }

                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    val fallbackUri =
                        "https://www.google.com/maps/dir/?api=1&destination=${data.spot.latitude},${data.spot.longitude}".toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                }
            },
            onReviewClick = viewModel::openReview,
            onUserProfileClick = {}
        )
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
            .padding(24.dp)
    ) {
        // 📷 Image
        spotDetails.spot.photoUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = "Spot photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(16.dp))
        }

        // 🌍 Type + cleanliness
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = spotDetails.spot.type.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = spotDetails.spot.type.toDisplayName(),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            CleanlinessChip(spotDetails.spot.cleanliness)
        }

        // 📝 Description
        spotDetails.spot.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 🙋‍♂️ User
        spotDetails.user?.let { user ->
            Spacer(Modifier.height(16.dp))
            PostedByCard(user = user, onUserProfileClick = onUserProfileClick)
        }

        Spacer(Modifier.height(24.dp))

        // 📍 Buttons
        ActionsButtons(
            onNavigateClick = onNavigateClick,
            onReviewClick = onReviewClick
        )
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
                Location.distanceBetween(
                    it.latitude, it.longitude,
                    spotLatLng.latitude, spotLatLng.longitude,
                    result
                )
                result[0]
            }
        }
        val isInZone = distanceMeters?.let { it <= 50f } ?: false

        Text(
            text = if (isInZone) "You are inside the 50m zone — you can review spot"
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

        Spacer(Modifier.height(8.dp))
    }
}