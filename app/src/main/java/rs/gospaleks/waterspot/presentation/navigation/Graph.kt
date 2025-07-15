package rs.gospaleks.waterspot.presentation.navigation

sealed class Graph(val route: String) {
    object Auth : Graph("auth")
    object Main : Graph("main")
}
