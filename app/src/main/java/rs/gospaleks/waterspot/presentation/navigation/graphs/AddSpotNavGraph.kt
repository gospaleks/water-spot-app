package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.AddSpotRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotPhotoScreen
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotScreen
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotViewModel
import rs.gospaleks.waterspot.presentation.screens.settings.SettingsScreen

fun NavGraphBuilder.addSpotNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.ADD_SPOT_GRAPH,
        startDestination = AddSpotRouteScreen.AddSpot.route
    ) {
        composable(AddSpotRouteScreen.AddSpot.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                rootNavHostController.getBackStackEntry(Graph.ADD_SPOT_GRAPH)
            }
            val viewModel = hiltViewModel<AddSpotViewModel>(parentEntry)

            AddSpotScreen(
                viewModel = viewModel,
                onBackClick = {
                    rootNavHostController.popBackStack()
                },
                onNextClick = {
                    rootNavHostController.navigate(AddSpotRouteScreen.AddSpotPhoto.route)
                }
            )
        }
        composable(AddSpotRouteScreen.AddSpotPhoto.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                rootNavHostController.getBackStackEntry(Graph.ADD_SPOT_GRAPH)
            }
            val viewModel = hiltViewModel<AddSpotViewModel>(parentEntry)

            AddSpotPhotoScreen(
                viewModel = viewModel,
                onBackClick = {
                    rootNavHostController.popBackStack()
                },
                onNextClick = {
                    rootNavHostController.navigate(AddSpotRouteScreen.AddSpotDetails.route)
                }
            )
        }
        composable(AddSpotRouteScreen.AddSpotDetails.route) {
            // TODO: will be implemented later
        }
    }
}