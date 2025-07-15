package rs.gospaleks.waterspot.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import rs.gospaleks.waterspot.presentation.navigation.Screen

@Composable
fun MainScaffold(
    navController: NavHostController,
    showBottomBar: Boolean,
    content: @Composable () -> Unit
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showFab = currentRoute == Screen.Map.route

    Scaffold (
        bottomBar = {
            if (showBottomBar) {
//                BottomNavigationBar(navController)
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = { /* TODO */ }) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = "Add Spot")
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

