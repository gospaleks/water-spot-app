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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

private enum class FilterSheet { Type, Cleanliness, Radius }

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
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Bottom sheet host state
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var currentSheet by rememberSaveable { mutableStateOf<FilterSheet?>(null) }

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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (searchResults.isEmpty()) {
                        item { NoResultsRow() }
                    } else {
                        items(searchResults.size) { idx ->
                            val item = searchResults[idx]
                            val spot = item.spot
                            val isFirst = idx == 0
                            val isLast = idx == searchResults.lastIndex
                            SearchSuggestionItem(
                                isFirst = isFirst,
                                isLast = isLast,
                                title = spot.description?.ifBlank { spot.type.toDisplayName() } ?: spot.type.toDisplayName(),
                                type = spot.type,
                                cleanliness = spot.cleanliness,
                                author = item.user?.fullName,
                                onClick = {
                                    val queryToApply = spot.description?.takeIf { it.isNotBlank() } ?: spot.type.name
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

        Spacer(Modifier.height(8.dp))

        // Gmail-like filters row -> LazyRow with chips + clear icon
        val hasActiveFilters = selectedTypes.isNotEmpty() || selectedCleanliness.isNotEmpty() || radiusMeters != DEFAULT_RADIUS_METERS
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
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
                if (hasActiveFilters) {
                    IconButton(
                        onClick = onClearAllFilters,
                        enabled = true
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear filters"
                        )
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
    return if (meters < 1000) "${meters} m" else "${meters / 1000} km"
}