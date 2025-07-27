package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import rs.gospaleks.waterspot.presentation.screens.map.GoogleMapScreen
import rs.gospaleks.waterspot.presentation.screens.profile.ProfileScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.navigation.MainRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.SettingsRouteScreen

@Composable
fun MainNavGraph(
    rootNavHostController: NavHostController,   // Navigira na rute van bottom bar navigacije
    homeNavController: NavHostController,       // navigira unutar bottom bar navigacije
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = homeNavController,
        route = Graph.MAIN_SCREEN_GRAPH,
        startDestination = MainRouteScreen.Map.route
    ) {
        composable (route = MainRouteScreen.Map.route) {
            GoogleMapScreen(modifier)
        }
        composable (route = MainRouteScreen.AllSpots.route) {
            // AllSpotsScreen()
        }
        composable (route = MainRouteScreen.Scoreboard.route) {
            // ScoreboardScreen()
        }
        composable (route = MainRouteScreen.Profile.route) {
             ProfileScreen(
                 modifier = modifier,
                 onSettingsNavigation = {
                     rootNavHostController.navigate(SettingsRouteScreen.Settings.route)
                 }
             )
        }
    }
}