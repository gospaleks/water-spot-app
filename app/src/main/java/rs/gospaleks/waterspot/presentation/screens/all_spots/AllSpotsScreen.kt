package rs.gospaleks.waterspot.presentation.screens.all_spots

import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import rs.gospaleks.waterspot.presentation.components.card.SpotCard
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheet
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheetViewModel
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllSpotsScreen(
    rootNavHostController: NavHostController,
    outerPadding: PaddingValues,
    viewModel: AllSpotsViewModel = hiltViewModel()
) {
    val bottomSheetViewModel: SpotDetailsBottomSheetViewModel = hiltViewModel()

    val uiState = viewModel.uiState

    // Pull to refresh
    val refreshState = rememberPullToRefreshState()

    // Re-fetch spots on every screen entry (ON_START)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.observeSpots(viewModel.uiState.radiusMeters)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column (
        modifier = Modifier
            .padding(bottom = outerPadding.calculateBottomPadding())
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SearchAndFilter(
            textFieldState = viewModel.textFieldState,
            // Pass full items for rich suggestions
            searchResults = uiState.filteredSpots,
            selectedTypes = uiState.selectedTypeFilters,
            onToggleType = viewModel::toggleTypeFilter,
            selectedCleanliness = uiState.selectedCleanlinessFilters,
            onToggleCleanliness = viewModel::toggleCleanlinessFilter,
            radiusMeters = uiState.radiusMeters,
            onRadiusMetersChange = viewModel::updateRadiusMeters,
            onRadiusChangeFinished = viewModel::applyRadiusChange,
            onQueryChange = viewModel::setSearchQuery,
            onClearAllFilters = viewModel::clearAllFilters,
        )

        // Lightweight loading bar for background refresh (e.g., radius change)
        if (uiState.isLoading && uiState.filteredSpots.isNotEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Content area (list/loading/empty)
        when {
            uiState.isLoading && uiState.filteredSpots.isEmpty() -> {
                // Shimmer skeleton list (only for initial load when list is empty)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(6) { index ->
                        ShimmerSpotCardPlaceholder(modifier = if (index == 0) Modifier.padding(top = 8.dp) else Modifier)
                    }
                }
            }
            uiState.filteredSpots.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(outerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.all_spots_empty_message),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            else -> {
                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = {
                        viewModel.refresh()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = outerPadding.calculateStartPadding(LayoutDirection.Ltr)),
                ) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(
                            items = uiState.filteredSpots,
                            key = { it.spot.id }
                        ) { spotWithUser ->
                            val index = uiState.filteredSpots.indexOf(spotWithUser)

                            SpotCard(
                                spotWithUser = spotWithUser,
                                modifier = (if (index == 0) Modifier.padding(top = 8.dp) else Modifier)
                                    .animateItem(),
                                onCardClick = {
                                    bottomSheetViewModel.onSpotClick(spotWithUser)
                                },
                                onUserClick = { userId ->
                                    if (userId.isNotBlank()) {
                                        Log.d("AllSpotsScreen", "Navigating to user profile: $userId")
                                        rootNavHostController.navigate(
                                            ProfileRouteScreen.PublicProfile.createRoute(userId)
                                        )
                                    } else {
                                        Log.w("AllSpotsScreen", "User ID is blank, cannot navigate to profile")
                                    }
                                },
                            )

                        }
                    }
                }
            }
        }
    }

    SpotDetailsBottomSheet(rootNavHostController = rootNavHostController, viewModel = bottomSheetViewModel)
}

@Composable
private fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    return Brush.linearGradient(
        colors = listOf(base.copy(alpha = 0.6f), highlight, base.copy(alpha = 0.6f)),
        start = Offset(translate - 200f, 0f),
        end = Offset(translate, 0f)
    )
}

@Composable
private fun ShimmerSpotCardPlaceholder(modifier: Modifier = Modifier) {
    val shimmer = rememberShimmerBrush()
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .height(110.dp)
            .padding(12.dp)
    ) {
        // Left image placeholder
        Box(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(shimmer)
        )

        Spacer(Modifier.width(12.dp))

        // Right content placeholders
        Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
            // Title row placeholder
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(120.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(70.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Second line placeholder
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer row placeholder
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }
        }
    }
}
