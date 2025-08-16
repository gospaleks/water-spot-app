package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import rs.gospaleks.waterspot.R

@Composable
fun ActionsButtons(
    onNavigateClick: () -> Unit,
    onReviewClick: () -> Unit,
    onAddPhotoClick: () -> Unit = {},
    isAddPhotoEnabled: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        ActionItem(
            icon = Icons.Default.Navigation,
            label = stringResource(R.string.spot_details_navigate_button),
            enabled = true,
            onClick = onNavigateClick,
            modifier = Modifier.weight(1f)
        )

        ActionItem(
            icon = Icons.Default.RateReview,
            label = stringResource(R.string.spot_details_review_button),
            enabled = true,
            onClick = onReviewClick,
            modifier = Modifier.weight(1f)
        )

        ActionItem(
            icon = Icons.Default.AddAPhoto,
            label = stringResource(R.string.spot_details_add_photo_button),
            enabled = isAddPhotoEnabled,
            onClick = onAddPhotoClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Transparent clickable surface for ripple + semantics
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = Color.Transparent,
        contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        modifier = modifier
            .heightIn(min = 64.dp)
            .semantics { role = Role.Button }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
        }
    }
}