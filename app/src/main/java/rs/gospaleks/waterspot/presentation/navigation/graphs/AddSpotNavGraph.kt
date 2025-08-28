package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.navigation.AddSpotRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotDetailsScreen
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotPhotoScreen
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotScreen
import rs.gospaleks.waterspot.presentation.screens.add_spot.AddSpotViewModel

fun NavGraphBuilder.addSpotNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.ADD_SPOT_GRAPH,
        startDestination = AddSpotRouteScreen.AddSpot.route
    ) {
        composable(
            route = AddSpotRouteScreen.AddSpot.route,
            enterTransition = {
                // When returning back from AddSpotPhoto -> slide in from left
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                // When going forward to AddSpotPhoto -> slide out to left
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                // When popping back to this screen -> slide in from left
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                // When leaving this screen by back navigation -> slide out to right
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { backStackEntry ->
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
        composable(
            route = AddSpotRouteScreen.AddSpotPhoto.route,
            enterTransition = {
                // From AddSpot -> slide in from right
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                // To AddSpotDetails -> slide out to left
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                // Coming back from AddSpotDetails -> slide in from left
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                // Back to AddSpot -> slide out to right
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { backStackEntry ->
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
        composable(
            route = AddSpotRouteScreen.AddSpotDetails.route,
            enterTransition = {
                // From AddSpotPhoto -> slide in from right
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                // On leaving details forward (unlikely inside this graph) -> keep consistent
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                // When popping back from next (or leaving details) -> slide in from left
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                // Back to AddSpotPhoto -> slide out to right
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                rootNavHostController.getBackStackEntry(Graph.ADD_SPOT_GRAPH)
            }
            val viewModel = hiltViewModel<AddSpotViewModel>(parentEntry)

            AddSpotDetailsScreen(
                viewModel = viewModel,
                onBackClick = {
                    rootNavHostController.popBackStack()
                },
                onSubmitSuccess = {
                    // Navigate back to the root graph after successful submission
                    // This will pop all the back stack entries in the ADD_SPOT_GRAPH
                    // and navigate to the root graph
                    rootNavHostController.navigate(Graph.ROOT_GRAPH) {
                        popUpTo(Graph.ADD_SPOT_GRAPH) { inclusive = true }
                    }
                }
            )
        }
    }
}