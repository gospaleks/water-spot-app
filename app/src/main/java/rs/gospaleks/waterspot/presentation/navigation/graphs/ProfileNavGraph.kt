package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen
import rs.gospaleks.waterspot.presentation.screens.profile.EditProfileScreen

fun NavGraphBuilder.profileNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.PROFILE_GRAPH,
        startDestination = ProfileRouteScreen.EditProfile.route
    ) {
        composable (ProfileRouteScreen.EditProfile.route) {
            EditProfileScreen(
                onBackClick = {
                    rootNavHostController.popBackStack()
                }
            )
        }

        // TODO: Dodaj dinamicke ekrane za javne profile korisnika
    }
}