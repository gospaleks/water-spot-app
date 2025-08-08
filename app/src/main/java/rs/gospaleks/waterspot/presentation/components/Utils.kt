package rs.gospaleks.waterspot.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import rs.gospaleks.waterspot.domain.model.CleanlinessLevelEnum
import rs.gospaleks.waterspot.domain.model.SpotTypeEnum
import rs.gospaleks.waterspot.domain.model.toStringResId
import rs.gospaleks.waterspot.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SpotTypeEnum.toDisplayName(): String = stringResource(id = toStringResId())

@Composable
fun CleanlinessLevelEnum.toDisplayName(): String = stringResource(id = toStringResId())

@Composable
fun SpotTypeEnum.icon(): ImageVector {
    return when (this) {
        SpotTypeEnum.FOUNTAIN -> ImageVector.vectorResource(id = R.drawable.ic_fountain_type)
        SpotTypeEnum.PUBLIC -> ImageVector.vectorResource(id = R.drawable.ic_public_type)
        SpotTypeEnum.REFILL_STATION -> Icons.Default.LocalDrink
        SpotTypeEnum.OTHER -> Icons.AutoMirrored.Filled.HelpOutline
    }
}

@Composable
fun CleanlinessLevelEnum.icon(): ImageVector {
    return when (this) {
        CleanlinessLevelEnum.CLEAN -> Icons.Default.EmojiEmotions
        CleanlinessLevelEnum.MODERATE -> Icons.Default.ReportProblem
        CleanlinessLevelEnum.DIRTY -> Icons.Default.Delete
    }
}

@Composable
fun CleanlinessLevelEnum.getColor(): Color {
    return when (this) {
        CleanlinessLevelEnum.CLEAN -> Color(0xFF4CAF50) // Green
        CleanlinessLevelEnum.MODERATE -> Color(0xFFFF9800) // Orange
        CleanlinessLevelEnum.DIRTY -> Color(0xFFF44336) // Red
    }
}

// Helper function to format date
fun formatDate(timestamp: Long?): String {
    if (timestamp == null) return ""
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return formatter.format(date)
}