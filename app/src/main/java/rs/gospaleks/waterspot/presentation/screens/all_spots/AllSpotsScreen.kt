package rs.gospaleks.waterspot.presentation.screens.all_spots

import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
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
import rs.gospaleks.waterspot.presentation.components.card.ShimmerSpotCardPlaceholder
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
    val filteredSpots by viewModel.filteredSpots

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
            searchResults = filteredSpots,
            selectedTypes = uiState.selectedTypeFilters,
            onToggleType = viewModel::toggleTypeFilter,
            selectedCleanliness = uiState.selectedCleanlinessFilters,
            onToggleCleanliness = viewModel::toggleCleanlinessFilter,
            radiusMeters = uiState.radiusMeters,
            onRadiusMetersChange = viewModel::updateRadiusMeters,
            onRadiusChangeFinished = viewModel::applyRadiusChange,
            onQueryChange = viewModel::setSearchQuery,
            onClearAllFilters = viewModel::clearAllFilters,
            // Date filter wiring
            dateFilterPreset = uiState.dateFilterPreset,
            customStartDateMillis = uiState.customStartDateMillis,
            customEndDateMillis = uiState.customEndDateMillis,
            onSetDatePreset = viewModel::setDatePreset,
            onSetCustomDateRange = viewModel::setCustomDateRange,
            // Sort
            sortBy = uiState.sortBy,
            onSetSortBy = viewModel::setSortBy,
        )

        // Lightweight loading bar for background refresh (e.g., radius change)
        if (uiState.isLoading && filteredSpots.isNotEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Content area (list/loading/empty)
        when {
            uiState.isLoading && filteredSpots.isEmpty() -> {
                // Shimmer skeleton list (only for initial load when list is empty)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = outerPadding.calculateStartPadding(LayoutDirection.Ltr)),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(6) { index ->
                        ShimmerSpotCardPlaceholder()
                    }
                }
            }
            filteredSpots.isEmpty() -> {
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
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = filteredSpots,
                            key = { it.spot.id },
                            contentType = { "spot_card" }
                        ) { spotWithUser ->
                            SpotCard(
                                spotWithUser = spotWithUser,
                                modifier = Modifier
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