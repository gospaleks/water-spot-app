package rs.gospaleks.waterspot.presentation.screens.auth.forgot_password

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import rs.gospaleks.waterspot.presentation.components.UiEvent
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.SendPasswordResetEmailUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateEmailUseCase

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val successMessage: String? = null,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
) : ViewModel() {
    var uiState by mutableStateOf(ForgotPasswordUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    fun onEmailChange(newEmail: String) {
        uiState = uiState.copy(email = newEmail)
    }

    fun onSend() {
        uiState = uiState.copy(
            emailError = null,
            isLoading = true
        )

        val emailResult = validateEmailUseCase(uiState.email)
        if (!emailResult.successful) {
            uiState = uiState.copy(
                emailError = "Invalid email address",
                isLoading = false
            )
            return
        }

        viewModelScope.launch {
            val result = sendPasswordResetEmailUseCase(uiState.email)
            if (result.isSuccess) {
                eventFlow.emit(UiEvent.ShowToast("Password reset email sent"))
                uiState = uiState.copy(
                    isLoading = false,
                    successMessage = "If an account with that email exists, a password reset email has been sent."
                )
            } else {
                eventFlow.emit(UiEvent.ShowToast(result.exceptionOrNull()?.localizedMessage ?: "An error occurred"))
                uiState = uiState.copy(isLoading = false)
            }
        }
    }
}