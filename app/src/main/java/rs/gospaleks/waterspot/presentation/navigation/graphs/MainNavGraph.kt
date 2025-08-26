package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import rs.gospaleks.waterspot.presentation.screens.map.GoogleMapScreen
import rs.gospaleks.waterspot.presentation.screens.profile.ProfileScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.navigation.MainRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen
import rs.gospaleks.waterspot.presentation.screens.all_spots.AllSpotsScreen
import rs.gospaleks.waterspot.presentation.screens.scoreboard.ScoreboardScreen

@Composable
fun MainNavGraph(
    rootNavHostController: NavHostController,   // Navigira na rute van bottom bar navigacije
    homeNavController: NavHostController,       // navigira unutar bottom bar navigacije
    innerPadding: PaddingValues,
    onLogout: () -> Unit,
) {
    NavHost(
        navController = homeNavController,
        route = Graph.MAIN_SCREEN_GRAPH,
        startDestination = MainRouteScreen.Map.route
    ) {
        composable (route = MainRouteScreen.Map.route) {
            GoogleMapScreen(
                rootNavHostController = rootNavHostController,
                navigateToAddSpotScreen = {
                    rootNavHostController.navigate(Graph.ADD_SPOT_GRAPH)
                },
                outerPadding = innerPadding
            )
        }
        composable (route = MainRouteScreen.AllSpots.route) {
            AllSpotsScreen(
                rootNavHostController = rootNavHostController,
                outerPadding = innerPadding
            )
        }
        composable (route = MainRouteScreen.Scoreboard.route) {
            ScoreboardScreen(
                rootNavHostController = rootNavHostController,
                outerPadding = innerPadding
            )
        }
        composable (route = MainRouteScreen.Profile.route) {
            ProfileScreen(
                innerPadding = innerPadding,
                onLogout = onLogout,
                onMyProfileClick = { userId ->
                    rootNavHostController.navigate(ProfileRouteScreen.PublicProfile.createRoute(userId))
                },
                onEditProfileClick = {
                    // TODO
                },
                onVisitedSpotsClick = {
                    rootNavHostController.navigate(ProfileRouteScreen.VisitedSpots.route)
                },
                onChangePasswordClick = {
                    rootNavHostController.navigate(ProfileRouteScreen.ChangePassword.route)
                },
            )
        }
    }
}