package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen
import rs.gospaleks.waterspot.presentation.screens.profile.change_password.ChangePasswordScreen
import rs.gospaleks.waterspot.presentation.screens.profile.visited_spots.VisitedSpotsScreen
import rs.gospaleks.waterspot.presentation.screens.public_profile.PublicProfileScreen

fun NavGraphBuilder.profileNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.PROFILE_GRAPH,
        startDestination = ProfileRouteScreen.EditProfile.route
    ) {
        composable (ProfileRouteScreen.EditProfile.route) {
            // TODO: Implement EditProfileScreen (display all info and allow chaning name and phone number)
        }

        composable (ProfileRouteScreen.ChangePassword.route) {
            ChangePasswordScreen(
                onBackClick = {
                    rootNavHostController.popBackStack()
                }
            )
        }

        composable(
            route = ProfileRouteScreen.PublicProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            PublicProfileScreen(
                rootNavHostController = rootNavHostController,
                userId = userId ?: "",
                onBackClick = {
                    rootNavHostController.popBackStack()
                }
            )
        }

        composable(route = ProfileRouteScreen.VisitedSpots.route) {
            VisitedSpotsScreen(
                rootNavHostController = rootNavHostController,
                onBackClick = {
                    rootNavHostController.popBackStack()
                }
            )
        }
    }
}