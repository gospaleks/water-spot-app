package rs.gospaleks.waterspot.presentation.auth.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase
import javax.inject.Inject
import rs.gospaleks.waterspot.R

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validateLoginPasswordUseCase: ValidateLoginPasswordUseCase
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onPasswordVisibilityChange() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun login() = viewModelScope.launch {
        val email = uiState.email
        val password = uiState.password

        val emailValidationResult = validateEmailUseCase(email)
        val passwordValidationResult = validateLoginPasswordUseCase(password)

        val hasEmailError = !emailValidationResult.successful
        val hasPasswordError = !passwordValidationResult.successful
        val hasError = hasEmailError || hasPasswordError

        if (hasError) {
            uiState = uiState.copy(
                emailError = if (hasEmailError) getErrorMessageFromType(emailValidationResult.errorType!!) else null,
                passwordError = if (hasPasswordError) getErrorMessageFromType(passwordValidationResult.errorType!!) else null
            )

            return@launch
        }

        uiState = uiState.copy(isLoading = true)

        // Simulate login process
        delay(2000)

        uiState = uiState.copy(
            isLoading = false,
            emailError = null,
            passwordError = null
        )
    }

    private fun getErrorMessageFromType(errorType: ValidationErrorType): Int {
        return when (errorType) {
            is ValidationErrorType.EmptyEmail -> R.string.error_empty_email
            is ValidationErrorType.InvalidEmailFormat -> R.string.error_invalid_email
            is ValidationErrorType.EmptyPassword -> R.string.error_empty_password
            is ValidationErrorType.ShortPassword -> R.string.error_short_password
        }
    }
}