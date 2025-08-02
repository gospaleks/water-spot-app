package rs.gospaleks.waterspot.presentation.screens.auth.login

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.model.ValidationErrorType
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateLoginPasswordUseCase
import javax.inject.Inject
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.auth.use_case.LoginUseCase
import rs.gospaleks.waterspot.presentation.screens.auth.UiEvent

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validateLoginPasswordUseCase: ValidateLoginPasswordUseCase,
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
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

        val result = loginUseCase(email, password)
        if (result.isSuccess) {
            eventFlow.emit(UiEvent.NavigateToHome)
        } else {
            eventFlow.emit(UiEvent.Error)
            Log.e("LoginViewModel", "Login failed: ${result.exceptionOrNull()?.message}")
        }

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