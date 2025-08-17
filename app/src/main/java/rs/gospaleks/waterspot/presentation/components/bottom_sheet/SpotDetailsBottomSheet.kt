package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import rs.gospaleks.waterspot.R
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.AppTheme
import rs.gospaleks.waterspot.presentation.components.UiEvent
import rs.gospaleks.waterspot.presentation.screens.profile.ThemeViewModel
import android.location.Location
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SpotDetailsBottomSheet(
    rootNavHostController: NavHostController,
    viewModel: SpotDetailsBottomSheetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    canClickOnAuthor: Boolean = true,
) {
    val uiState = viewModel.uiState

    // Prepare camera launcher for Add Photo
    val context = LocalContext.current
    val pendingPhotoUri = remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingPhotoUri.value
        if (success && uri != null) {
            viewModel.addAdditionalPhotoToSpot(uri)
        }
        // Clear after attempt
        pendingPhotoUri.value = null
    }

    fun createImageUri(): Uri? {
        val contentValues = ContentValues().apply {
            val name = "waterspot_${System.currentTimeMillis()}.jpg"
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // Compute distance and zone status (shared by Details and Review screens)
    val (distanceMeters, isInZone) = remember(uiState.userLocation, uiState.selectedSpot) {
        val user = uiState.userLocation
        val spot = uiState.selectedSpot?.spot
        if (user != null && spot != null) {
            val result = FloatArray(1)
            Location.distanceBetween(
                user.latitude, user.longitude,
                spot.latitude, spot.longitude,
                result
            )
            result[0] to (result[0] <= 50f)
        } else null to false
    }

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // Change map style based on theme
    val myTheme by themeViewModel.appTheme.collectAsState(initial = AppTheme.SYSTEM)

    val isDark = when (myTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> isSystemInDarkTheme()
    }
    val mapStyleJson = if (isDark) {
        MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_dark)
    } else {
        MapStyleOptions.loadRawResourceStyle(LocalContext.current, R.raw.map_light)
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

    // Toast messages
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.ShowToast) {
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val scope = rememberCoroutineScope()

    if (uiState.selectedSpot == null) { return }

    if (uiState.isModalOpen) {
        val saveableStateHolder = rememberSaveableStateHolder()

        ModalBottomSheet(
            onDismissRequest = viewModel::dismissBottomSheet,
            sheetState = sheetState,
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .imePadding()
        ) {
            AnimatedContent(targetState = uiState.sheetMode) { mode ->
                when (mode) {
                    BottomSheetMode.DETAILS -> {
                        val stateKey = "spot_details_${uiState.selectedSpot.spot.id}"
                        saveableStateHolder.SaveableStateProvider(stateKey) {
                            SpotDetailsContent(
                                data = uiState.selectedSpot,
                                reviews = uiState.reviews,
                                isLoading = uiState.isLoading,
                                onReviewClick = viewModel::openReview,
                                onNavigateClick = {
                                    val data = uiState.selectedSpot

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
                                onUserProfileClick = {
                                    if (canClickOnAuthor) {
                                        // Zatvori modal i navigiraj na javni profil
                                        scope.launch {
                                            sheetState.hide()
                                            rootNavHostController.navigate("public_profile/${uiState.selectedSpot.spot.userId}")
                                        }
                                    } else {
                                        // Sakrij modal
                                        scope.launch {
                                            sheetState.hide()
                                            viewModel.dismissBottomSheet()
                                        }
                                    }
                                },
                                onAddPhotoClick = {
                                    // Only proceed if enabled (also guarded in ActionsButtons)
                                    val uri = createImageUri()
                                    if (uri != null) {
                                        pendingPhotoUri.value = uri
                                        cameraLauncher.launch(uri)
                                    }
                                },
                                isAddPhotoEnabled = isInZone,
                                isUploadingPhoto = uiState.isUploadingPhoto,
                                onReviewerProfileClick = { reviewerId ->
                                    scope.launch {
                                        sheetState.hide()
                                        rootNavHostController.navigate(
                                            ProfileRouteScreen.PublicProfile.createRoute(reviewerId)
                                        )                                }
                                }
                            )
                        }
                    }
                    BottomSheetMode.REVIEW -> {
                        ReviewContent(
                            data = uiState.selectedSpot,
                            userLocation = uiState.userLocation,
                            mapStyleJson = mapStyleJson,
                            onBack = viewModel::openDetails,
                            onSubmitReview = { reviewText, rating ->
                                viewModel.submitReview(
                                    reviewText = reviewText,
                                    rating = rating
                                )
                            },
                            isInZone = isInZone,
                            distanceMeters = distanceMeters
                        )
                    }
                }
            }
        }
    }
}