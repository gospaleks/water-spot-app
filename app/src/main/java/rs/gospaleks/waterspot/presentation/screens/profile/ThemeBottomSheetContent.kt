package rs.gospaleks.waterspot.presentation.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.domain.model.AppTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R

@Composable
fun ThemeBottomSheetContent(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.theme),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        AppTheme.entries.forEach { theme ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        MaterialTheme.shapes.large
                    )
                    .clickable {
                        onThemeSelected(theme)
                    }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = theme == selectedTheme,
                    onClick = {
                        onThemeSelected(theme)
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when (theme) {
                        AppTheme.LIGHT -> stringResource(R.string.theme_light)
                        AppTheme.DARK -> stringResource(R.string.theme_dark)
                        AppTheme.SYSTEM -> stringResource(R.string.theme_system)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}


