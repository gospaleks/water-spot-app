package rs.gospaleks.waterspot.presentation.screens.auth.login

data class LoginUiState(
    val email: String = "",
    val emailError: Int? = null,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordError: Int? = null,
    val isLoading: Boolean = false,
)