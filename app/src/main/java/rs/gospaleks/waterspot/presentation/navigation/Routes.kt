package rs.gospaleks.waterspot.presentation.navigation

object Graph {
    const val ROOT_GRAPH = "root_graph"
    const val AUTH_GRAPH = "auth_graph"
    const val MAIN_SCREEN_GRAPH = "main_screen_graph"
    const val ADD_SPOT_GRAPH = "add_spot_graph"
    const val PROFILE_GRAPH = "profile_graph"
}

sealed class AuthRouteScreen(val route: String) {
    object Welcome : AuthRouteScreen("welcome")
    object Login : AuthRouteScreen("login")
    object Register : AuthRouteScreen("register")
}

sealed class MainRouteScreen(val route: String) {
    object Map : MainRouteScreen("map")
    object AllSpots : MainRouteScreen("all_spots")
    object Scoreboard : MainRouteScreen("scoreboard")
    object Profile : MainRouteScreen("profile")
}

sealed class AddSpotRouteScreen(val route: String) {
    object AddSpot : AddSpotRouteScreen("add_spot")
    object AddSpotPhoto: AddSpotRouteScreen("add_spot_photo")
    object AddSpotDetails: AddSpotRouteScreen("add_spot_details")
}

sealed class ProfileRouteScreen(val route: String) {
    object EditProfile : ProfileRouteScreen("edit_profile")
    object PublicProfile : ProfileRouteScreen("public_profile/{userId}") {
        fun createRoute(userId: String) = "public_profile/$userId"
    }
}