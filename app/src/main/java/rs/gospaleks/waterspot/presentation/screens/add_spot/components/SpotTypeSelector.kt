package rs.gospaleks.waterspot.presentation.screens.add_spot.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum

data class SpotType(
    val label: String,
    val icon: ImageVector
)

@Composable
fun SpotTypeSelector(
    selectedType: SpotTypeEnum?, // ✅ promenjeno sa String na enum
    onTypeSelected: (SpotTypeEnum) -> Unit // ✅ promenjeno
) {
    val types = listOf(
        SpotType(
            label = stringResource(R.string.add_spot_details_type_fountain),
            icon = ImageVector.vectorResource(id = R.drawable.ic_fountain_type)
        ) to SpotTypeEnum.FOUNTAIN,

        SpotType(
            label = stringResource(R.string.add_spot_details_type_public),
            icon = ImageVector.vectorResource(id = R.drawable.ic_public_type)
        ) to SpotTypeEnum.PUBLIC,

        SpotType(
            label = stringResource(R.string.add_spot_details_type_refill_station),
            icon = Icons.Default.LocalDrink
        ) to SpotTypeEnum.REFILL_STATION,

        SpotType(
            label = stringResource(R.string.add_spot_details_type_other),
            icon = Icons.AutoMirrored.Filled.HelpOutline
        ) to SpotTypeEnum.OTHER
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        for (row in types.chunked(2)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { (type, typeEnum) ->
                    val isSelected = selectedType == typeEnum
                    ElevatedCard(
                        onClick = { onTypeSelected(typeEnum) },
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = type.icon,
                                contentDescription = type.label,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpotTypeSelectorPreview() {
    MaterialTheme {
        SpotTypeSelector(
            selectedType = SpotTypeEnum.FOUNTAIN,
            onTypeSelected = {}
        )
    }
}