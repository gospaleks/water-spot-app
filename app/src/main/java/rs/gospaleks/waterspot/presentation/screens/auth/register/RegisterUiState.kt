package rs.gospaleks.waterspot.presentation.screens.auth.register

import android.net.Uri

data class RegisterUiState (
    val photoUri: Uri? = null,
    val fullName: String = "",
    val fullNameError: Int? = null,
    val email: String = "",
    val emailError: Int? = null,
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val passwordError: Int? = null,
    val phoneNumber: String = "",
    val phoneNumberError: Int? = null,
    val isLoading: Boolean = false,
    val isPhotoSelected: Boolean = false
)