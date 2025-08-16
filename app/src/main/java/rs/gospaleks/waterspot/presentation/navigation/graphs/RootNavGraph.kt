package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.screens.auth.AuthUiState
import rs.gospaleks.waterspot.presentation.screens.auth.AuthViewModel
import rs.gospaleks.waterspot.presentation.screens.main.MainScreen

@Composable
fun RootNavGraph(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val rootNavController = rememberNavController()
    val startDestination = if (viewModel.isUserLoggedInState is AuthUiState.Authenticated) {
        Graph.MAIN_SCREEN_GRAPH
    } else {
        Graph.AUTH_GRAPH
    }

    NavHost(
        navController = rootNavController,
        route = Graph.ROOT_GRAPH,
        startDestination = startDestination,
        modifier = Modifier.background(MaterialTheme.colorScheme.background)
    ) {
        composable (route = Graph.MAIN_SCREEN_GRAPH) {
            MainScreen(
                rootNavHostController = rootNavController,
                onLogout = {
                    rootNavController.navigate(Graph.AUTH_GRAPH) {
                        popUpTo(Graph.ROOT_GRAPH) { inclusive = true }
                    }
                    viewModel.logout()
                },
            )
        }
        authNavGraph(rootNavHostController = rootNavController)
        profileNavGraph(rootNavHostController = rootNavController)
        addSpotNavGraph(rootNavHostController = rootNavController)

        // Napomena: Sa rootNavController se upravlja kroz sve ekrane
    }
}