package rs.gospaleks.waterspot.presentation.navigation

object Graph {
    const val ROOT_GRAPH = "root_graph"
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_SCREEN_GRAPH = "main_screen_graph"
    const val ADD_SPOT_GRAPH = "add_spot_graph"
    const val SETTINGS_GRAPH = "settings_graph"
    // Other graphs here
}

sealed class AuthRouteScreen(val route: String) {
    object Welcome : AuthRouteScreen("welcome")
    object Login : AuthRouteScreen("login")
    object Register : AuthRouteScreen("register")
    // more authentication-related screens here (e.g., ForgotPassword, ResetPassword...)
}

sealed class MainRouteScreen(val route: String) {
    object Map : MainRouteScreen("map")
    object AllSpots : MainRouteScreen("all_spots")
    object Scoreboard : MainRouteScreen("scoreboard")
    object Profile : MainRouteScreen("profile")
}

sealed class AddSpotRouteScreen(val route: String) {
    object AddSpot : AddSpotRouteScreen("add_spot")
    // more screens related to adding spots here (e.g., AddSpotDetails, AddSpotConfirmation...)
}

sealed class SettingsRouteScreen(val route: String) {
    object Settings : SettingsRouteScreen("settings")
    // more settings-related screens here
}
