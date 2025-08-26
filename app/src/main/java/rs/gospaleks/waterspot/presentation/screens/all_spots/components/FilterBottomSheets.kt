package rs.gospaleks.waterspot.presentation.screens.all_spots.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.presentation.components.getColor
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.screens.all_spots.DateFilterPreset
import rs.gospaleks.waterspot.presentation.screens.all_spots.SortByOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TypeFilterBottomSheetContent(
    selectedTypes: Set<SpotTypeEnum>,
    onToggleType: (SpotTypeEnum) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = "Type", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Non-scrollable list; parent provides scrolling
        SpotTypeEnum.entries.forEach { type ->
            FilterListItem(
                checked = selectedTypes.contains(type),
                onCheckedChange = { onToggleType(type) },
                leading = { Icon(imageVector = type.icon(), contentDescription = null) },
                title = type.toDisplayName()
            )
        }
    }
}

@Composable
fun CleanlinessFilterBottomSheetContent(
    selectedCleanliness: Set<CleanlinessLevelEnum>,
    onToggleCleanliness: (CleanlinessLevelEnum) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = "Cleanliness", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // Non-scrollable list; parent provides scrolling
        CleanlinessLevelEnum.entries.forEach { level ->
            FilterListItem(
                checked = selectedCleanliness.contains(level),
                onCheckedChange = { onToggleCleanliness(level) },
                leading = {
                    Icon(
                        imageVector = level.icon(),
                        contentDescription = null,
                        tint = level.getColor()
                    )
                },
                title = level.toDisplayName(),
                titleColor = level.getColor()
            )
        }
    }
}

@Composable
fun RadiusFilterBottomSheetContent(
    currentMeters: Int,
    onMetersChange: (Int) -> Unit,
    onApply: () -> Unit
) {
    val currentKm = (currentMeters / 1000).coerceAtLeast(1)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "Radius - ${formatRadius(currentMeters)}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(Modifier.height(8.dp))

        // Slider 1..100 km
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "1 km")
            Slider(
                modifier = Modifier.weight(1f),
                value = currentKm.toFloat(),
                onValueChange = { km -> onMetersChange(km.toInt().coerceIn(1, 200) * 1000) },
                valueRange = 1f..200f,
                steps = 199,
                onValueChangeFinished = { onApply() }
            )
            Text(text = "200 km")
        }

        Spacer(Modifier.height(12.dp))

        // Quick picks - meters
        Text(text = "Meters", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(50, 100, 250, 500, 750, 1000).forEach { m ->
                AssistChip(
                    onClick = {
                        onMetersChange(m)
                        onApply()
                    },
                    label = { Text("$m m") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (currentMeters == m) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick picks - kilometers
        Text(text = "Kilometers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(2, 5, 10, 20, 50, 100, 200).forEach { km ->
                AssistChip(
                    onClick = {
                        onMetersChange(km * 1000)
                        onApply()
                    },
                    label = { Text("$km km") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (currentMeters == km * 1000) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFilterBottomSheetContent(
    selectedPreset: DateFilterPreset,
    onPresetChange: (DateFilterPreset) -> Unit,
    onCustomRangeSelected: (Long?, Long?) -> Unit,
    // New: preselect dates
    currentStartDateMillis: Long?,
    currentEndDateMillis: Long?,
) {
    var showDateRangePicker by remember { mutableStateOf(false) }

    if (showDateRangePicker) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = currentStartDateMillis,
            initialSelectedEndDateMillis = currentEndDateMillis
        )
        val fmt = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
        val headlineText = remember(rangeState.selectedStartDateMillis, rangeState.selectedEndDateMillis) {
            val start = rangeState.selectedStartDateMillis
            val end = rangeState.selectedEndDateMillis
            fun format(millis: Long): String = fmt.format(Date(millis))
            when {
                start != null && end != null -> "${format(start)} - ${format(end)}"
                start != null -> "${format(start)} - End date"
                else -> "Start date - End date"
            }
        }
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCustomRangeSelected(rangeState.selectedStartDateMillis, rangeState.selectedEndDateMillis)
                        showDateRangePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDateRangePicker = false
                    onPresetChange(if (currentStartDateMillis != null || currentEndDateMillis != null) DateFilterPreset.CUSTOM else DateFilterPreset.ANY)
                }) { Text("Cancel") }
            }
        ) {
            DateRangePicker(
                state = rangeState,
                title = {},
                headline = {
                    Text(
                        text = headlineText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 24.dp)
                    )
                },
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = "Date", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        PresetRadioRow(
            title = "Any time",
            selected = selectedPreset == DateFilterPreset.ANY,
            onClick = { onPresetChange(DateFilterPreset.ANY) }
        )
        PresetRadioRow(
            title = "Older than a week",
            selected = selectedPreset == DateFilterPreset.OLDER_WEEK,
            onClick = { onPresetChange(DateFilterPreset.OLDER_WEEK) }
        )
        PresetRadioRow(
            title = "Older than a month",
            selected = selectedPreset == DateFilterPreset.OLDER_MONTH,
            onClick = { onPresetChange(DateFilterPreset.OLDER_MONTH) }
        )
        PresetRadioRow(
            title = "Older than 6 months",
            selected = selectedPreset == DateFilterPreset.OLDER_6_MONTHS,
            onClick = { onPresetChange(DateFilterPreset.OLDER_6_MONTHS) }
        )
        PresetRadioRow(
            title = "Older than a year",
            selected = selectedPreset == DateFilterPreset.OLDER_YEAR,
            onClick = { onPresetChange(DateFilterPreset.OLDER_YEAR) }
        )

        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = {
                onPresetChange(DateFilterPreset.CUSTOM)
                showDateRangePicker = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Custom range")
        }
    }
}

@Composable
fun SortByBottomSheetContent(
    selected: SortByOption,
    onSelectedChange: (SortByOption) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(text = "Sort by", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        PresetRadioRow(
            title = "Updated: Newest first",
            selected = selected == SortByOption.UPDATED_DESC,
            onClick = { onSelectedChange(SortByOption.UPDATED_DESC) }
        )
        PresetRadioRow(
            title = "Updated: Oldest first",
            selected = selected == SortByOption.UPDATED_ASC,
            onClick = { onSelectedChange(SortByOption.UPDATED_ASC) }
        )
        PresetRadioRow(
            title = "Created: Newest first",
            selected = selected == SortByOption.CREATED_DESC,
            onClick = { onSelectedChange(SortByOption.CREATED_DESC) }
        )
        PresetRadioRow(
            title = "Created: Oldest first",
            selected = selected == SortByOption.CREATED_ASC,
            onClick = { onSelectedChange(SortByOption.CREATED_ASC) }
        )
    }
}

@Composable
private fun PresetRadioRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FilterListItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    leading: @Composable (() -> Unit)? = null,
    title: String,
    titleColor: Color? = null
) {
    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onCheckedChange(!checked) },
        headlineContent = {
            Text(text = title, color = titleColor ?: LocalContentColor.current)
        },
        leadingContent = leading,
        trailingContent = {
            Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

private fun formatRadius(meters: Int): String {
    return if (meters < 1000) "$meters m" else "${meters / 1000} km"
}
