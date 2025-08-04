package rs.gospaleks.waterspot.presentation.screens.main

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import rs.gospaleks.waterspot.presentation.navigation.BottomNavigationBar
import rs.gospaleks.waterspot.presentation.navigation.MainRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.graphs.MainNavGraph

@Composable
fun MainScreen(
    rootNavHostController: NavHostController,
    homeNavController: NavHostController = rememberNavController(),
    onLogout: () -> Unit,
) {
    Scaffold (
        bottomBar = {
            BottomNavigationBar(homeNavController)
        },
    ) { innerPadding ->
        MainNavGraph(
            rootNavHostController = rootNavHostController,
            homeNavController = homeNavController,
            innerPadding = innerPadding,
            onLogout = onLogout,
        )
    }
}