package rs.gospaleks.waterspot.presentation.components

import rs.gospaleks.waterspot.presentation.ui.theme.success
import rs.gospaleks.waterspot.presentation.ui.theme.warning
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class AlertType { INFO, WARNING, ERROR, SUCCESS }

@Composable
fun AlertCard(
    modifier: Modifier = Modifier,
    type: AlertType = AlertType.INFO,
    message: String,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row (
            modifier = Modifier
                .background(
                    color = when (type) {
                        AlertType.INFO -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        AlertType.WARNING -> warning.copy(alpha = 0.1f)
                        AlertType.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        AlertType.SUCCESS -> success.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (type) {
                AlertType.INFO -> Icons.Default.Info
                AlertType.WARNING -> Icons.Default.Warning
                AlertType.ERROR -> Icons.Default.Error
                AlertType.SUCCESS -> Icons.Default.CheckCircle
            }
            val color = when (type) {
                AlertType.INFO -> MaterialTheme.colorScheme.primary
                AlertType.WARNING -> warning
                AlertType.ERROR -> MaterialTheme.colorScheme.error
                AlertType.SUCCESS -> success
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = color,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}