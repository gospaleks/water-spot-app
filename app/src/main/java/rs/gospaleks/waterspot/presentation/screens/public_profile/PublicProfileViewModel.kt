package rs.gospaleks.waterspot.presentation.screens.public_profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.UserWithSpots
import rs.gospaleks.waterspot.domain.use_case.GetUserWithSpotsUseCase
import javax.inject.Inject

data class PublicProfileUiState(
    val userId: String = "",
    val userWithspots: UserWithSpots? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val getUserWithSpotsUseCase: GetUserWithSpotsUseCase
) : ViewModel() {
    var uiState by mutableStateOf(PublicProfileUiState())
        private set

    fun loadUserWithSpots(userId: String) = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true, userId = userId)

        getUserWithSpotsUseCase(userId).collect { result ->
            result.onSuccess { userWithSpots ->
                uiState = uiState.copy(userWithspots = userWithSpots, isLoading = false)
            }.onFailure { exception ->
                uiState = uiState.copy(error = exception.message, isLoading = false)
            }
        }
    }
}