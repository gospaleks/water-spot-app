package rs.gospaleks.waterspot.presentation.navigation

sealed class Screen(val route: String) {
    // Authentication screens
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main screens for bottom navigation bar
    object Map : Screen("map")
    object AllSpots : Screen("all_spots")
    object Scoreboard : Screen("scoreboard")
    object Profile : Screen("profile")
}
