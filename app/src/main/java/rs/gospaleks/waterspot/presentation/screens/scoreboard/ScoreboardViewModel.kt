package rs.gospaleks.waterspot.presentation.screens.scoreboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.domain.use_case.GetAllUsersUseCase
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class ScoreboardUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    private val getAllUsersUseCase: GetAllUsersUseCase
) : ViewModel() {

    var uiState by mutableStateOf(ScoreboardUiState())
        private set

    init {
        observeUsers()
    }

    private fun observeUsers() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        getAllUsersUseCase().collect { result ->
            result
                .onSuccess { users ->
                    uiState = uiState.copy(
                        users = users,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    uiState = uiState.copy(
                        error = exception.message,
                        isLoading = false
                    )
                }
        }
    }
}