package rs.gospaleks.waterspot.presentation.navigation

import rs.gospaleks.waterspot.R
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import rs.gospaleks.waterspot.presentation.auth.AuthUiState
import rs.gospaleks.waterspot.presentation.auth.AuthViewModel
import rs.gospaleks.waterspot.presentation.auth.login.LoginScreen
import rs.gospaleks.waterspot.presentation.auth.RegisterScreen
import rs.gospaleks.waterspot.presentation.auth.WelcomeScreen
import rs.gospaleks.waterspot.presentation.main.components.BottomNavigationBar
import rs.gospaleks.waterspot.presentation.main.map.GoogleMapScreen
import rs.gospaleks.waterspot.presentation.main.profile.ProfileScreen
import rs.gospaleks.waterspot.presentation.main.settings.SettingsScreen
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState = viewModel.isUserLoggedInState

    when (authState) {
        is AuthUiState.Authenticated -> {
            MainNavHost(navController, onLogout = { viewModel.logout() })
        }
        is AuthUiState.Unauthenticated -> {
            AuthNavHost(
                navController,
                onLoginSuccess = { viewModel.setAuthState(AuthUiState.Authenticated) },
                onRegisterSuccess = { /* TODO handle register success */ }
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun AuthNavHost(
    navController: NavHostController,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(
            Screen.Login.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = onLoginSuccess,
            )
        }
        composable(
            Screen.Register.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() },
                onRegisterSuccess = onRegisterSuccess,
            )
        }
    }
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Map.route, Screen.Profile.route)

    Scaffold (
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.AddSpot.route) },
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_spot_fab_content_description))
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // With Bottom Nav Bar and FAB
            composable(Screen.Map.route) {
                GoogleMapScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen()
            }

            // Without Bottom Nav Bar and FAB
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}