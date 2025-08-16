package rs.gospaleks.waterspot.presentation.screens.all_spots.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.presentation.components.getColor
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName

@Composable
fun TypeFilterBottomSheetContent(
    selectedTypes: Set<SpotTypeEnum>,
    onToggleType: (SpotTypeEnum) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(text = "Type", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(SpotTypeEnum.entries) { type ->
                FilterListItem(
                    checked = selectedTypes.contains(type),
                    onCheckedChange = { onToggleType(type) },
                    leading = {
                        Icon(imageVector = type.icon(), contentDescription = null)
                    },
                    title = type.toDisplayName()
                )
            }
        }
    }
}

@Composable
fun CleanlinessFilterBottomSheetContent(
    selectedCleanliness: Set<CleanlinessLevelEnum>,
    onToggleCleanliness: (CleanlinessLevelEnum) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(text = "Cleanliness", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(CleanlinessLevelEnum.entries) { level ->
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
}

@Composable
fun RadiusFilterBottomSheetContent(
    currentMeters: Int,
    onMetersChange: (Int) -> Unit,
    onApply: () -> Unit
) {
    val currentKm = (currentMeters / 1000).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(text = "Radius", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text(text = "Selected: ${formatRadius(currentMeters)}", style = MaterialTheme.typography.bodyMedium)
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
                onValueChange = { km -> onMetersChange(km.toInt().coerceIn(1, 100) * 1000) },
                valueRange = 1f..100f,
                steps = 98
            )
            Text(text = "100 km")
        }

        Spacer(Modifier.height(12.dp))

        // Quick picks - meters
        Text(text = "Meters", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(100, 250, 500, 750, 1000).forEach { m ->
                AssistChip(
                    onClick = { onMetersChange(m) },
                    label = { Text("${m} m") },
                    colors = AssistChipDefaults.assistChipColors()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Quick picks - kilometers
        Text(text = "Kilometers", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 5, 10, 20, 50, 100).forEach { km ->
                AssistChip(
                    onClick = { onMetersChange(km * 1000) },
                    label = { Text("$km km") }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Apply") }
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
