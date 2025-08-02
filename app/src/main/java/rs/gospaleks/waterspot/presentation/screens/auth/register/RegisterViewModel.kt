package rs.gospaleks.waterspot.presentation.screens.auth.register

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.RegisterUseCase
import rs.gospaleks.waterspot.presentation.screens.auth.UiEvent
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    var uiState by mutableStateOf(RegisterUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    fun onPhotoCaptured(uri: Uri) {
        uiState = uiState.copy(
            photoUri = uri,
            isPhotoSelected = true
        )
    }

    fun onFullNameChange(fullName: String) {
        uiState = uiState.copy(fullName = fullName)
    }

    fun onEmailChange(email: String) {
        uiState = uiState.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        uiState = uiState.copy(password = password)
    }

    fun onPasswordVisibilityChange() {
        uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible)
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        uiState = uiState.copy(phoneNumber = phoneNumber)
    }

    fun register() = viewModelScope.launch {
        val fullName = uiState.fullName
        val email = uiState.email
        val password = uiState.password
        val phoneNumber = uiState.phoneNumber
        val photoUri = uiState.photoUri

        // TODO: Validacija unetih podataka

        uiState = uiState.copy(isLoading = true)

        val result = registerUseCase(
            fullName = fullName,
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            photoUri = photoUri // Ako se prosledi null, koristice se default avatar
        )

        if (result.isSuccess) {
            eventFlow.emit(UiEvent.NavigateToHome)
        } else {
            eventFlow.emit(UiEvent.Error)
            Log.e("RegisterViewModel", "Login failed: ${result.exceptionOrNull()?.message}")
        }

        uiState = uiState.copy(
            isLoading = false,
            fullNameError = null,
            emailError = null,
            passwordError = null,
            phoneNumberError = null
        )
    }
}