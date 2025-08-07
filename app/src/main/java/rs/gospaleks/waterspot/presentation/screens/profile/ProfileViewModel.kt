package rs.gospaleks.waterspot.presentation.screens.profile

import android.net.Uri
import android.util.Log
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
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.use_case.UploadAvatarUseCase

data class ProfileUiState(
    val user: User = User(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAvatarUploading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {
    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        loadUserData()
    }

    fun uploadAvatar(imageUri: Uri) = viewModelScope.launch {
        uiState = uiState.copy(isAvatarUploading = true, errorMessage = null)

        val uploadResult = uploadAvatarUseCase(imageUri)

        Log.d("ProfileViewModel", "Upload result: $uploadResult")

        uiState = if (uploadResult.isSuccess) {
            uiState.copy(
                user = uiState.user.copy(profilePictureUrl = uploadResult.getOrNull()),
                isAvatarUploading = false,
                errorMessage = null
            )
        } else {
            uiState.copy(
                isAvatarUploading = false,
                errorMessage = "Failed to upload avatar: ${uploadResult.exceptionOrNull()?.message}"
            )
        }
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

        Log.d("ProfileViewModel", "User data result: $userDataResult")

        if (userDataResult.isSuccess) {
            val userData = userDataResult.getOrNull()
            uiState = if (userData != null) {
                uiState.copy(
                    user = userData,
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