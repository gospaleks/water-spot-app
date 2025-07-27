package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.navigation.SettingsRouteScreen
import rs.gospaleks.waterspot.presentation.screens.settings.SettingsScreen

fun NavGraphBuilder.settingsNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.SETTINGS_GRAPH,
        startDestination = SettingsRouteScreen.Settings.route
    ) {
        composable(SettingsRouteScreen.Settings.route) {
            SettingsScreen()
        }
    }
}