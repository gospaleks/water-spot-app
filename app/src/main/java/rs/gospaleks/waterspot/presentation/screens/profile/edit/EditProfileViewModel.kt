package rs.gospaleks.waterspot.presentation.screens.profile.edit

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rs.gospaleks.waterspot.domain.use_case.user.GetUserDataUseCase
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.ValidatePhoneNumberUseCase
import rs.gospaleks.waterspot.domain.use_case.user.UpdateUserDataUseCase
import rs.gospaleks.waterspot.presentation.components.UiEvent

data class EditProfileUiState(
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserDataUseCase: GetUserDataUseCase,
    private val updateUserDataUseCase: UpdateUserDataUseCase,
    private val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase
) : ViewModel() {

    var uiState by mutableStateOf(EditProfileUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    init {
        observeUserData()
    }

    fun onFullNameChange(newName: String) {
        uiState = uiState.copy(fullName = newName)
    }

    fun onPhoneNumberChange(newPhoneNumber: String) {
        uiState = uiState.copy(phoneNumber = newPhoneNumber)
    }

    fun onSaveChanges() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        val phoneNumberValidationResult = validatePhoneNumberUseCase(uiState.phoneNumber)
        if (!phoneNumberValidationResult.successful) {
            uiState = uiState.copy(isLoading = false)
            eventFlow.emit(UiEvent.ShowToast("Invalid phone number"))
            return@launch
        }

        val saveChangesResult = updateUserDataUseCase(
            fullName = uiState.fullName,
            phoneNumber = uiState.phoneNumber
        )

        if (saveChangesResult.isSuccess) {
            uiState = uiState.copy(isLoading = false)
            eventFlow.emit(UiEvent.ShowToast("Profile updated successfully"))
        } else {
            uiState = uiState.copy(isLoading = false)
            eventFlow.emit(UiEvent.ShowToast("Error updating profile data"))
        }
    }

    private fun observeUserData() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        val uidResult = getCurrentUserUseCase()

        val uid = uidResult.getOrNull()
        if (uid == null) {
            uiState = uiState.copy(isLoading = false)
            eventFlow.emit(UiEvent.ShowToast("Error fetching user data"))
            return@launch
        }

        getUserDataUseCase(uid).collect { result ->
            result.onSuccess { user ->
                uiState = uiState.copy(
                    email = user.email,
                    fullName = user.fullName,
                    phoneNumber = user.phoneNumber,
                    isLoading = false
                )
            }.onFailure {
                uiState = uiState.copy(isLoading = false)
                eventFlow.emit(UiEvent.ShowToast("Error fetching user data"))
            }
        }
    }
}