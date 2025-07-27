package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.AddSpotRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotScreen
import rs.gospaleks.waterspot.presentation.screens.settings.SettingsScreen

fun NavGraphBuilder.addSpotNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.ADD_SPOT_GRAPH,
        startDestination = AddSpotRouteScreen.AddSpot.route
    ) {
        composable(AddSpotRouteScreen.AddSpot.route) {
            AddSpotScreen()
        }
    }
}