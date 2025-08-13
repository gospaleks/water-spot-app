package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import rs.gospaleks.waterspot.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SpotDetailsBottomSheet(
    viewModel: SpotDetailsBottomSheetViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current

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

    if (uiState.selectedSpot == null) { return }

    if (uiState.isModalOpen) {
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
                            onUserProfileClick = { },
                        )
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
                            }
                        )
                    }
                }
            }
        }
    }
}