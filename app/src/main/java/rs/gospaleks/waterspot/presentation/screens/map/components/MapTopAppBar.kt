package rs.gospaleks.waterspot.presentation.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.presentation.components.ReusableBottomSheetHost
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.CleanlinessFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.RadiusFilterBottomSheetContent
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.TypeFilterBottomSheetContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar(
    // Centralized filters bottom sheet props
    selectedTypes: Set<SpotTypeEnum>,
    onToggleType: (SpotTypeEnum) -> Unit,
    selectedCleanliness: Set<CleanlinessLevelEnum>,
    onToggleCleanliness: (CleanlinessLevelEnum) -> Unit,
    radiusMeters: Int,
    onRadiusMetersChange: (Int) -> Unit,
    onRadiusApply: () -> Unit,
    onClearAllFilters: () -> Unit,
    activeFiltersCount: Int,
) {
    var isFilterSheetOpen by remember { mutableStateOf(false) }

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
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
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
            BadgedBox(
                modifier = Modifier.padding(end = 8.dp),
                badge = {
                    if (activeFiltersCount > 0) {
                        Badge { Text(activeFiltersCount.toString()) }
                    }
                }
            ) {
                IconButton(onClick = { isFilterSheetOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters"
                    )
                }
            }
        }
    )

    ReusableBottomSheetHost(
        show = isFilterSheetOpen,
        onDismissRequest = { isFilterSheetOpen = false },
        sheetContent = {
            // Header row with title and Clear All action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stringResource(R.string.filter_title), style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onClearAllFilters, enabled = activeFiltersCount > 0) {
                    Text("Clear all")
                }
            }
            // Scrollable content sections
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 12.dp)
                    .navigationBarsPadding()
            ) {
                // Type section
                TypeFilterBottomSheetContent(
                    selectedTypes = selectedTypes,
                    onToggleType = onToggleType
                )

                Spacer(Modifier.height(4.dp))

                // Cleanliness section
                CleanlinessFilterBottomSheetContent(
                    selectedCleanliness = selectedCleanliness,
                    onToggleCleanliness = onToggleCleanliness
                )

                Spacer(Modifier.height(4.dp))

                // Radius section
                RadiusFilterBottomSheetContent(
                    currentMeters = radiusMeters,
                    onMetersChange = onRadiusMetersChange,
                    onApply = { onRadiusApply() }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { isFilterSheetOpen = false },
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                ) { Text("Done") }
            }
        },
        allowPartial = true,
        initialPartial = true
    )
}
