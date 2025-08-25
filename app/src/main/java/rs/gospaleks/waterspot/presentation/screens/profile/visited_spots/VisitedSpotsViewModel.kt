package rs.gospaleks.waterspot.presentation.screens.profile.visited_spots

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.UiEvent
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.use_case.GetVisitedSpotsUseCase

data class VisitedSpotsUiState(
    val visitedSpots: List<SpotWithUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class VisitedSpotsViewModel @Inject constructor(
    private val getVisitedSpotsUseCase: GetVisitedSpotsUseCase
) : ViewModel() {
    var uiState by mutableStateOf(VisitedSpotsUiState())
        private set

    var eventFlow = MutableSharedFlow<UiEvent>()
        private set

    init {
        loadVisitedSpots()
    }

    private fun loadVisitedSpots() = viewModelScope.launch {
        uiState = uiState.copy(isLoading = true, error = null)

        val result = getVisitedSpotsUseCase()

        if (result.isSuccess) {
            uiState = uiState.copy(
                visitedSpots = result.getOrDefault(emptyList()),
                isLoading = false
            )
        } else {
            uiState = uiState.copy(
                isLoading = false,
                error = result.exceptionOrNull()?.localizedMessage ?: "An unexpected error occurred"
            )
            eventFlow.emit(UiEvent.ShowToast(uiState.error ?: "An unexpected error occurred"))
        }
    }
}