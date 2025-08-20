package rs.gospaleks.waterspot.presentation.screens.profile.change_password

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rs.gospaleks.waterspot.domain.auth.use_case.ChangePasswordUseCase
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.model.getErrorMessageFromType
import rs.gospaleks.waterspot.domain.auth.use_case.ValidateRegisterPasswordUseCase

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = ""
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val validateRegisterPasswordUseCase: ValidateRegisterPasswordUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    var uiState by mutableStateOf(ChangePasswordUiState())
        private set

    fun onCurrentPasswordChange(newPassword: String) {
        uiState = uiState.copy(currentPassword = newPassword)
    }

    fun onNewPasswordChange(newPassword: String) {
        uiState = uiState.copy(newPassword = newPassword)
    }

    fun onConfirmNewPasswordChange(newPassword: String) {
        uiState = uiState.copy(confirmNewPassword = newPassword)
    }

    fun changePassword() {
        if (uiState.newPassword != uiState.confirmNewPassword) {
            uiState = uiState.copy(error = "New passwords do not match")
            return
        }

        val passwordValidationResult = validateRegisterPasswordUseCase(
            password = uiState.newPassword
        )
        if (!passwordValidationResult.successful) {
            // TODO: Change to resource id like in register screen
            uiState = uiState.copy(
                error = "Password must contain at least one uppercase letter, one lowercase letter and one digit"
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, successMessage = null)

            val result = changePasswordUseCase(currentPassword = uiState.currentPassword, newPassword = uiState.newPassword)

            uiState = if (result.isSuccess) {
                uiState.copy(
                    isLoading = false,
                    successMessage = "Password changed successfully",
                    currentPassword = "",
                    newPassword = "",
                    confirmNewPassword = ""
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "An error occurred"
                )
            }
        }
    }
}