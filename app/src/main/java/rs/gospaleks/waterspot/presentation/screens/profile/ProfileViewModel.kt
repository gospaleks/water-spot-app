package rs.gospaleks.waterspot.presentation.screens.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rs.gospaleks.waterspot.domain.use_case.GetUserDataUseCase
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.auth.use_case.GetCurrentUserUseCase

data class ProfileUiState(
    val userFullName: String = "",
    val userPhoneNumber: String = "",
    val userProfileImage: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadUserData()
    }

    private fun loadUserData() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        val currentUserIdResult = getCurrentUserUseCase()
        val uid = currentUserIdResult.getOrNull()

        if (uid == null) {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Failed to get current user ID."
            )
            return@launch
        }

        val userDataResult = getUserDataUseCase(uid)

        if (userDataResult.isSuccess) {
            val userData = userDataResult.getOrNull()
            uiState = if (userData != null) {
                uiState.copy(
                    userFullName = userData.fullName,
                    userPhoneNumber = userData.phoneNumber,
                    userProfileImage = userData.profileImage,
                    isLoading = false
                )
            } else {
                uiState.copy(
                    isLoading = false,
                    errorMessage = "User data not found."
                )
            }
        } else {
            uiState = uiState.copy(
                isLoading = false,
                errorMessage = "Failed to load user data."
            )
        }
    }
}