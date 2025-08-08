package rs.gospaleks.waterspot.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum

@Composable
fun CleanlinessChip(
    cleanliness: CleanlinessLevelEnum,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = cleanliness.getColor().copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = cleanliness.icon(),
                contentDescription = null,
                tint = cleanliness.getColor(),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = cleanliness.toDisplayName(),
                style = MaterialTheme.typography.labelMedium,
                color = cleanliness.getColor()
            )
        }
    }
}