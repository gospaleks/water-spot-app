package rs.gospaleks.waterspot.presentation.screens.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
) {

    Scaffold (
        topBar = {
            BasicTopAppBar(
                title = stringResource(id = R.string.edit_profile_title),
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(
                text = "Edit profile screen content goes here.",
            )
        }
    }
}