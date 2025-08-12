package rs.gospaleks.waterspot.presentation.components

sealed class UiEvent {
    object NavigateToHome : UiEvent()

    object Error : UiEvent()
    data class ShowToast(val message: String) : UiEvent()
}