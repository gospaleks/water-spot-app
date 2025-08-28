package rs.gospaleks.waterspot.presentation.navigation.graphs

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import rs.gospaleks.waterspot.presentation.screens.auth.register.RegisterScreen
import rs.gospaleks.waterspot.presentation.screens.auth.WelcomeScreen
import rs.gospaleks.waterspot.presentation.screens.auth.login.LoginScreen
import rs.gospaleks.waterspot.presentation.navigation.AuthRouteScreen
import rs.gospaleks.waterspot.presentation.navigation.Graph
import rs.gospaleks.waterspot.presentation.screens.auth.forgot_password.ForgotPasswordScreen

fun NavGraphBuilder.authNavGraph(
    rootNavHostController: NavHostController,
) {
    navigation(
        route = Graph.AUTH_GRAPH,
        startDestination = AuthRouteScreen.Welcome.route
    ) {
        composable(AuthRouteScreen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { rootNavHostController.navigate(AuthRouteScreen.Login.route) },
                onRegisterClick = { rootNavHostController.navigate(AuthRouteScreen.Register.route) }
            )
        }
        composable(
            AuthRouteScreen.Login.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            LoginScreen(
                onBackClick = { rootNavHostController.popBackStack() },
                onForgotPasswordClick = { rootNavHostController.navigate(AuthRouteScreen.ForgotPassword.route) },
                onLoginSuccess = {
                    rootNavHostController.navigate(Graph.MAIN_SCREEN_GRAPH) {
                        popUpTo(Graph.AUTH_GRAPH) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(
            AuthRouteScreen.Register.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(durationMillis = 400)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(durationMillis = 400)) }
        ) {
            RegisterScreen(
                onBackClick = { rootNavHostController.popBackStack() },
                onRegisterSuccess = {
                    rootNavHostController.navigate(Graph.MAIN_SCREEN_GRAPH) {
                        popUpTo(Graph.AUTH_GRAPH) {
                            inclusive = true
                        }
                    }
                },
            )
        }

        composable (
            AuthRouteScreen.ForgotPassword.route,
        ) {
            ForgotPasswordScreen (
                onBackClick = { rootNavHostController.popBackStack() },
            )
        }
    }
}