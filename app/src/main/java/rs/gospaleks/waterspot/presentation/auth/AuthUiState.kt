package rs.gospaleks.waterspot.presentation.auth

sealed class AuthUiState {
    object Authenticated : AuthUiState()
    object Unauthenticated : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}