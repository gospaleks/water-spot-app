package rs.gospaleks.waterspot.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
        startDestination = if (isUserLoggedIn) Graph.Main.route else Graph.Auth.route
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
                onLoginClick = {  },
                onRegisterClick = {  }
            )
        }
        composable(Screen.Login.route) {
        }
        composable(Screen.Register.route) {
        }
    }
}