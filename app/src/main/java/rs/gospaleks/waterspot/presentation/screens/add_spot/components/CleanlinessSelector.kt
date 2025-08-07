package rs.gospaleks.waterspot.presentation.screens.add_spot.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum

data class CleanlinessLevel(
    val id: CleanlinessLevelEnum,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun CleanlinessSelector(
    selected: CleanlinessLevelEnum?,
    onSelected: (CleanlinessLevelEnum) -> Unit
) {
    val levels = listOf(
        CleanlinessLevel(
            id = CleanlinessLevelEnum.CLEAN,
            label = stringResource(R.string.add_spot_details_cleanliness_clean),
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF4CAF50)
        ),
        CleanlinessLevel(
            id = CleanlinessLevelEnum.MODERATE,
            label = stringResource(R.string.add_spot_details_cleanliness_moderate),
            icon = Icons.Default.Warning,
            color = Color(0xFFFFC107)
        ),
        CleanlinessLevel(
            id = CleanlinessLevelEnum.DIRTY,
            label = stringResource(R.string.add_spot_details_cleanliness_dirty),
            icon = Icons.Default.Delete,
            color = Color(0xFFF44336)
        )
    )

    SingleChoiceSegmentedButtonRow (
        modifier = Modifier.fillMaxWidth()
    ) {
        levels.forEachIndexed { index, level ->
            val isSelected = selected == level.id

            SegmentedButton (
                selected = isSelected,
                onClick = { onSelected(level.id) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = levels.size),
                icon = {
                    Icon(
                        imageVector = level.icon,
                        contentDescription = level.label
                    )
                },
                label = {
                    Text(text = level.label)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCleanlinessSelector() {
    CleanlinessSelector(
        selected = CleanlinessLevelEnum.CLEAN,
        onSelected = {  }
    )
}
