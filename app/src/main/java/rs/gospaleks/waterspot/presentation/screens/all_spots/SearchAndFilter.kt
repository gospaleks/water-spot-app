package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndFilter(
    textFieldState: TextFieldState,
    // Changed: provide full items instead of just strings
    searchResults: List<SpotWithUser>,
    // Filters
    selectedTypes: Set<SpotTypeEnum>,
    onToggleType: (SpotTypeEnum) -> Unit,
    selectedCleanliness: Set<CleanlinessLevelEnum>,
    onToggleCleanliness: (CleanlinessLevelEnum) -> Unit,
    radiusKm: Int,
    onRadiusChange: (Int) -> Unit,
    onRadiusChangeFinished: () -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var filtersVisible by rememberSaveable { mutableStateOf(false) }

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
                        item {
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
                    } else {
                        items(searchResults.size) { idx ->
                            val item = searchResults[idx]
                            val spot = item.spot

                            val isFirst = idx == 0
                            val isLast = idx == searchResults.lastIndex

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        top = if (isFirst) 8.dp else 0.dp,
                                        bottom = if (isLast) 8.dp else 0.dp
                                    ).animateItem(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.background,
                                tonalElevation = 1.dp
                            ) {
                                ListItem(
                                    leadingContent = {
                                        Icon(
                                            imageVector = spot.type.icon(),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    headlineContent = {
                                        Text(
                                            text = spot.description?.ifBlank { spot.type.toDisplayName() } ?: spot.type.toDisplayName(),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    supportingContent = {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = spot.type.toDisplayName(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Icon(
                                                imageVector = spot.cleanliness.icon(),
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = spot.cleanliness.getColor()
                                            )
                                            Text(
                                                text = spot.cleanliness.toDisplayName(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = spot.cleanliness.getColor()
                                            )
                                            item.user?.let { user ->
                                                Text(
                                                    text = "• by ${user.fullName}",
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
                                        .clickable {
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
        }

        Spacer(Modifier.height(2.dp))

        // Filters toggle row (icon only)
        val rotation by animateFloatAsState(
            targetValue = if (filtersVisible) 180f else 0f,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "filtersIconRotation"
        )
        val iconTint by animateColorAsState(
            targetValue = if (filtersVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            animationSpec = tween(220, easing = FastOutSlowInEasing),
            label = "filtersIconTint"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Spot List")
            Row(
                modifier = Modifier.clickable { filtersVisible = !filtersVisible },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Filters")
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Toggle filters",
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }
        }

        AnimatedVisibility(
            visible = filtersVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(Modifier.height(8.dp))

                // Spot Types
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    val allTypes = SpotTypeEnum.entries
                    items(allTypes.size) { idx ->
                        val type = allTypes[idx]
                        FilterChip(
                            selected = selectedTypes.contains(type),
                            onClick = { onToggleType(type) },
                            label = { Text(type.toDisplayName()) },
                            leadingIcon = {
                                Icon(
                                    imageVector = type.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Cleanliness Levels
                val allLevels = CleanlinessLevelEnum.entries
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(allLevels.size) { idx ->
                        val level = allLevels[idx]
                        FilterChip(
                            selected = selectedCleanliness.contains(level),
                            onClick = { onToggleCleanliness(level) },
                            label = { Text(level.toDisplayName()) },
                            leadingIcon = {
                                Icon(
                                    imageVector = level.icon(),
                                    contentDescription = null,
                                    tint = level.getColor(),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                val radiusOptions = listOf(1, 2, 5, 10, 20, 30, 50)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    items(radiusOptions.size) { idx ->
                        val km = radiusOptions[idx]
                        FilterChip(
                            selected = radiusKm == km,
                            onClick = {
                                if (radiusKm != km) {
                                    onRadiusChange(km)
                                    onRadiusChangeFinished()
                                }
                            },
                            label = { Text("$km km") }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}