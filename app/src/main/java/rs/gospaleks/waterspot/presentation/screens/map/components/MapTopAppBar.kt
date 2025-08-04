package rs.gospaleks.waterspot.presentation.screens.map.components

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopAppBar() {
    CenterAlignedTopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
    )
}