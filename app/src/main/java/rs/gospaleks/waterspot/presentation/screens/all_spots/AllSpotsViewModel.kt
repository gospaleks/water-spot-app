package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.domain.use_case.GetAllSpotsWithUserUseCase
import javax.inject.Inject

data class AllSpotsUiState(
    val isLoading: Boolean = false,
    val spotsWithUser: List<SpotWithUser> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AllSpotsViewModel @Inject constructor(
    private val getAllSpotsWithUserUseCase: GetAllSpotsWithUserUseCase
) : ViewModel() {
    var uiState by mutableStateOf(AllSpotsUiState())
        private set

    init {
        observeSpots()
    }

    private fun observeSpots() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true)

        getAllSpotsWithUserUseCase().collect { result ->
            result
                .onSuccess { spots ->
                    uiState = uiState.copy(
                        spotsWithUser = spots,
                        isLoading = false,
                        error = null
                    )
                }
                .onFailure { error ->
                    uiState = uiState.copy(
                        isLoading = false,
                        spotsWithUser = emptyList(),
                        error = error.message ?: "Unknown error"
                    )
                }
        }
    }
}