package rs.gospaleks.waterspot.presentation.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import rs.gospaleks.waterspot.domain.auth.use_case.IsUserLoggedInUseCase
import rs.gospaleks.waterspot.domain.auth.use_case.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    var isUserLoggedInState by mutableStateOf<AuthUiState>(AuthUiState.Loading)

    init {
        checkUserLoggedInStatus()
    }

    fun setAuthState(state: AuthUiState) {
        isUserLoggedInState = state
    }

    fun logout() {
        logoutUseCase()
        checkUserLoggedInStatus()
    }

    private fun checkUserLoggedInStatus() {
        isUserLoggedInState = if (isUserLoggedInUseCase()) {
            AuthUiState.Authenticated
        } else {
            AuthUiState.Unauthenticated
        }
    }
}