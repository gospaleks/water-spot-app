package rs.gospaleks.waterspot.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.auth.login.LoginScreen
import rs.gospaleks.waterspot.presentation.auth.RegisterScreen
import rs.gospaleks.waterspot.presentation.auth.WelcomeScreen
import rs.gospaleks.waterspot.presentation.main.MainScaffold
import rs.gospaleks.waterspot.presentation.main.map.GoogleMapScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    isUserLoggedIn: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = if (isUserLoggedIn) Graph.Main.route else Graph.Auth.route,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        authGraph(navController)
        mainGraph(navController)
    }
}

private val bottomBarScreens: List<Pair<String, @Composable () -> Unit>> = listOf(
    Screen.Map.route to { GoogleMapScreen() },
//    Screen.AllSpots.route to { AllSpotsScreen() },
//    Screen.Scoreboard.route to { ScoreboardScreen() },
//    Screen.Profile.route to { ProfileScreen() }
)

fun NavGraphBuilder.mainGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Map.route,
        route = Graph.Main.route
    ) {
        // Ekrani sa BottomBar-om
        bottomBarScreens.forEach { (route, screenContent) ->
            composable(route) {
                MainScaffold(
                    navController = navController,
                    showBottomBar = true,
                    content = screenContent
                )
            }
        }

        // Ostali ekrani bez BottomBar-a (add spot, settings, etc.)
    }
}

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation(
        startDestination = Screen.Welcome.route,
        route = Graph.Auth.route
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route)},
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(
            Screen.Login.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            LoginScreen(
                onBackClick = { navController.navigateUp() },
                onLoginSuccess = {
                    navController.navigate(Graph.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            Screen.Register.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            RegisterScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}