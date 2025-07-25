package rs.gospaleks.waterspot.presentation.main.components

import rs.gospaleks.waterspot.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import rs.gospaleks.waterspot.presentation.navigation.Screen

@Composable
fun BottomNavigationBar(
    navController: NavHostController
)  {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(
            Screen.Map.route,
            Icons.Outlined.LocationOn,
            Icons.Filled.LocationOn,
            stringResource(R.string.map_label)
        ),
        BottomNavItem(
            Screen.AllSpots.route,
            Icons.AutoMirrored.Outlined.List,
            Icons.AutoMirrored.Filled.List,
            stringResource(R.string.all_spots_label)
        ),
        BottomNavItem(
            Screen.Scoreboard.route,
            Icons.Outlined.BarChart,
            Icons.Filled.BarChart,
            stringResource(R.string.scoreboard_label)
        ),
        BottomNavItem(
            Screen.Profile.route,
            Icons.Outlined.Person,
            Icons.Filled.Person,
            stringResource(R.string.profile_label)
        ),
    )

    NavigationBar (windowInsets = NavigationBarDefaults.windowInsets) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    if (currentRoute == item.route) {
                        Icon(
                            imageVector = item.filledIcon,
                            contentDescription = item.label
                        )
                    } else {
                        Icon(
                            imageVector = item.outlineIcon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(item.label)
                },

            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val outlineIcon: ImageVector,
    val filledIcon: ImageVector,
    val label: String
)
