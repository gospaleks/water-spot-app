package rs.gospaleks.waterspot.presentation.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val outlineIcon: ImageVector,
    val filledIcon: ImageVector,
    val label: String
)

