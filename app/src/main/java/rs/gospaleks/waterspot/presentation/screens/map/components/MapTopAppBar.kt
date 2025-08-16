package rs.gospaleks.waterspot.presentation.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.screens.map.MapFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar(
    currentFilters: MapFilters,
    onFilterApply: (MapFilters) -> Unit
) {
    var isFilterDialogOpen by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.logo_v1_providno_512),
                    contentDescription = stringResource(id = R.string.app_name),
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        actions = {
            IconButton(onClick = { isFilterDialogOpen = true }) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filters"
                )
            }
        }
    )

    if (isFilterDialogOpen) {
        // Privremeni filter state (lokalan samo dok je dijalog otvoren)
        var tempRadius by remember { mutableDoubleStateOf(currentFilters.radius) }

        AlertDialog(
            onDismissRequest = { isFilterDialogOpen = false },
            title = { Text(text = stringResource(R.string.filter_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Radius filter
                    Text("${stringResource(R.string.filter_radius_label)} ${radiusLabel(tempRadius)}")
                    Slider(
                        value = tempRadius.toFloat(),
                        onValueChange = { tempRadius = it.toDouble() },
                        valueRange = 50f..10_000f,
                        steps = 9
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onFilterApply(
                            MapFilters(
                                radius = tempRadius,
                            )
                        )
                        isFilterDialogOpen = false
                    }
                ) {
                    Text(stringResource(R.string.apply_button))
                }
            },
            dismissButton = {
                OutlinedButton (onClick = { isFilterDialogOpen = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

private fun radiusLabel(radius: Double): String {
    return if (radius >= 1000) {
        "${(radius / 1000).roundToInt()} km"
    } else {
        "${radius.roundToInt()} m"
    }
}
