package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.components.getColor
import rs.gospaleks.waterspot.presentation.components.ReusableBottomSheetHost
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.CleanlinessFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.RadiusFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.TypeFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.DateFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.SortByBottomSheetContent

private enum class FilterSheet { Type, Cleanliness, Radius, Date, SortBy }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilter(
    textFieldState: TextFieldState,
    searchResults: List<SpotWithUser>,
    selectedTypes: Set<SpotTypeEnum>,
    onToggleType: (SpotTypeEnum) -> Unit,
    selectedCleanliness: Set<CleanlinessLevelEnum>,
    onToggleCleanliness: (CleanlinessLevelEnum) -> Unit,
    radiusMeters: Int,
    onRadiusMetersChange: (Int) -> Unit,
    onRadiusChangeFinished: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearAllFilters: () -> Unit,
    // Date filter
    dateFilterPreset: DateFilterPreset,
    customStartDateMillis: Long?,
    customEndDateMillis: Long?,
    onSetDatePreset: (DateFilterPreset) -> Unit,
    onSetCustomDateRange: (Long?, Long?) -> Unit,
    // Sort by
    sortBy: SortByOption,
    onSetSortBy: (SortByOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Bottom sheet host state
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var currentSheet by rememberSaveable { mutableStateOf<FilterSheet?>(null) }

    var showSuggestions by remember { mutableStateOf(false) }

    LaunchedEffect(expanded) {
        if (expanded) {
            delay(200) // sačekaj da animacija završi da se rastereti lag
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }

    Column(
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .semantics { isTraversalGroup = true }
    ) {
        val targetPadding = if (expanded) 0.dp else 24.dp
        val animatedPadding by animateDpAsState(
            targetValue = targetPadding,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            label = "searchBarHorizontalPadding"
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = animatedPadding)
        ) {
            val queryString = textFieldState.text.toString()

            SearchBar(
                modifier = Modifier
                    .semantics { traversalIndex = 0f }
                    .fillMaxWidth(),
                inputField = {
                    SearchBarDefaults.InputField(
                        query = queryString,
                        onQueryChange = {
                            textFieldState.edit { replace(0, length, it) }
                            onQueryChange(it)
                        },
                        onSearch = {
                            onQueryChange(queryString)
                            expanded = false
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Search") },
                        leadingIcon = {
                            Crossfade(targetState = expanded, label = "leadingIconCrossfade") { isExpanded ->
                                if (isExpanded) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { expanded = false }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        trailingIcon = {
                            Crossfade(targetState = queryString.isNotEmpty(), label = "trailingIconCrossfade") { show ->
                                if (show) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                textFieldState.edit { replace(0, length, "") }
                                                onQueryChange("")
                                            }
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                // Fullscreen, scrollable suggestions area
                if (showSuggestions && queryString.isNotBlank()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (searchResults.isEmpty()) {
                            item { NoResultsRow() }
                        } else {
                            val maxItems = 30
                            val count =
                                if (searchResults.size > maxItems) maxItems else searchResults.size
                            items(
                                count = count,
                                key = { idx -> searchResults[idx].spot.id }) { idx ->
                                val item = searchResults[idx]
                                val spot = item.spot
                                val isFirst = idx == 0
                                val isLast = idx == count - 1
                                SearchSuggestionItem(
                                    isFirst = isFirst,
                                    isLast = isLast,
                                    title = spot.description?.ifBlank { spot.type.toDisplayName() }
                                        ?: spot.type.toDisplayName(),
                                    type = spot.type,
                                    cleanliness = spot.cleanliness,
                                    author = item.user?.fullName,
                                    onClick = {
                                        val queryToApply =
                                            spot.description?.takeIf { it.isNotBlank() }
                                                ?: spot.type.name
                                        textFieldState.edit { replace(0, length, queryToApply) }
                                        onQueryChange(queryToApply)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Gmail-like filters row -> LazyRow with chips + clear icon
        val hasActiveFilters = selectedTypes.isNotEmpty() || selectedCleanliness.isNotEmpty() || radiusMeters != DEFAULT_RADIUS_METERS || dateFilterPreset != DateFilterPreset.ANY || (customStartDateMillis != null || customEndDateMillis != null)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                FilterChip(
                    selected = selectedTypes.isNotEmpty(),
                    onClick = {
                        currentSheet = FilterSheet.Type
                        showBottomSheet = true
                    },
                    label = {
                        val count = selectedTypes.size
                        Text(if (count == 0) "Type" else "Type ($count)")
                    }
                )
            }
            item {
                FilterChip(
                    selected = selectedCleanliness.isNotEmpty(),
                    onClick = {
                        currentSheet = FilterSheet.Cleanliness
                        showBottomSheet = true
                    },
                    label = {
                        val count = selectedCleanliness.size
                        Text(if (count == 0) "Cleanliness" else "Cleanliness ($count)")
                    }
                )
            }
            item {
                FilterChip(
                    selected = radiusMeters != DEFAULT_RADIUS_METERS,
                    onClick = {
                        currentSheet = FilterSheet.Radius
                        showBottomSheet = true
                    },
                    label = {
                        Text("Radius: ${formatRadius(radiusMeters)}")
                    }
                )
            }
            item {
                val dateLabel = when (dateFilterPreset) {
                    DateFilterPreset.ANY -> "Date"
                    DateFilterPreset.OLDER_WEEK -> "Older than a week"
                    DateFilterPreset.OLDER_MONTH -> "Older than a month"
                    DateFilterPreset.OLDER_6_MONTHS -> "Older than 6 mont..."
                    DateFilterPreset.OLDER_YEAR -> "Older than a year"
                    DateFilterPreset.CUSTOM -> "Date: Custom"
                }

                val dateSelected = dateFilterPreset != DateFilterPreset.ANY || (customStartDateMillis != null || customEndDateMillis != null)

                FilterChip(
                    selected = dateSelected,
                    onClick = {
                        currentSheet = FilterSheet.Date
                        showBottomSheet = true
                    },
                    label = { Text(text = dateLabel) }
                )
            }
            item {
                val sortLabel = when (sortBy) {
                    SortByOption.UPDATED_DESC -> "Updated: Newest"
                    SortByOption.UPDATED_ASC -> "Updated: Oldest"
                    SortByOption.CREATED_DESC -> "Created: Newest"
                    SortByOption.CREATED_ASC -> "Created: Oldest"
                }

                FilterChip(
                    selected = true,
                    onClick = {
                        currentSheet = FilterSheet.SortBy
                        showBottomSheet = true
                    },
                    label = { Text(text = sortLabel) },
                )
            }
            item {
                if (hasActiveFilters) {
                    TextButton (
                        onClick = onClearAllFilters,
                        enabled = true
                    ) {
                        Text("Clear all")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bottom sheet host - renders with latest state
        ReusableBottomSheetHost(
            show = showBottomSheet,
            onDismissRequest = { showBottomSheet = false },
            sheetContent = {
                when (currentSheet) {
                    FilterSheet.Type -> TypeFilterBottomSheetContent(
                        selectedTypes = selectedTypes,
                        onToggleType = onToggleType,
                    )
                    FilterSheet.Cleanliness -> CleanlinessFilterBottomSheetContent(
                        selectedCleanliness = selectedCleanliness,
                        onToggleCleanliness = onToggleCleanliness,
                    )
                    FilterSheet.Radius -> RadiusFilterBottomSheetContent(
                        currentMeters = radiusMeters,
                        onMetersChange = onRadiusMetersChange,
                        onApply = { onRadiusChangeFinished() }
                    )
                    FilterSheet.Date -> DateFilterBottomSheetContent(
                        selectedPreset = dateFilterPreset,
                        onPresetChange = onSetDatePreset,
                        onCustomRangeSelected = { start, end -> onSetCustomDateRange(start, end) },
                        currentStartDateMillis = customStartDateMillis,
                        currentEndDateMillis = customEndDateMillis,
                    )
                    FilterSheet.SortBy -> SortByBottomSheetContent(
                        selected = sortBy,
                        onSelectedChange = { option ->
                            onSetSortBy(option)
                            showBottomSheet = false
                        }
                    )
                    null -> {}
                }
            }
        )
    }
}

@Composable
private fun NoResultsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "No results",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchSuggestionItem(
    isFirst: Boolean,
    isLast: Boolean,
    title: String,
    type: SpotTypeEnum,
    cleanliness: CleanlinessLevelEnum,
    author: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = if (isFirst) 8.dp else 0.dp,
                bottom = if (isLast) 8.dp else 0.dp
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 1.dp
    ) {
        ListItem(
            leadingContent = {
                Icon(
                    imageVector = type.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            },
            headlineContent = {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type.toDisplayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(
                        imageVector = cleanliness.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = cleanliness.getColor()
                    )
                    Text(
                        text = cleanliness.toDisplayName(),
                        style = MaterialTheme.typography.labelMedium,
                        color = cleanliness.getColor()
                    )
                    author?.let {
                        Text(
                            text = "• by $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        )
    }
}

private fun formatRadius(meters: Int): String {
    return if (meters < 1000) "$meters m" else "${meters / 1000} km"
}