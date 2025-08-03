package rs.gospaleks.waterspot.presentation.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.navigation.AddSpotRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.BottomNavigationBar
import rs.gospaleks.waterspot.presentation.navigation.MainRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.graphs.MainNavGraph

@Composable
fun MainScreen(
    rootNavHostController: NavHostController,
    homeNavController: NavHostController = rememberNavController(),
    onLogout: () -> Unit,
) {
    val backStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showFab = currentRoute == MainRouteScreen.Map.route

    Scaffold (
        bottomBar = {
            BottomNavigationBar(homeNavController, currentRoute)
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        rootNavHostController.navigate(AddSpotRouteScreen.AddSpot.route)
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_spot_fab_content_description))
                }
            }
        }
    ) { innerPadding ->
        MainNavGraph(
            rootNavHostController = rootNavHostController,
            homeNavController = homeNavController,
            innerPadding = innerPadding,
            onLogout = onLogout,
        )
    }
}