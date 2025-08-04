package rs.gospaleks.waterspot.presentation.screens.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.R

@Composable
fun CustomFABs(
    outerPadding: PaddingValues,
    cameraReset: () -> Unit,
    navigateToAddSpotScreen: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.End,
        modifier = Modifier.padding(bottom = outerPadding.calculateBottomPadding() - 16.dp) // Posto je scaffold u scaffold, mora se izbazdari
    ) {
        FloatingActionButton(
            onClick = { cameraReset() },
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Center map on current location"
            )
        }

        ExtendedFloatingActionButton(
            onClick = navigateToAddSpotScreen,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text(text = stringResource(R.string.add_spot_fab_content_description)) }
        )
    }
}