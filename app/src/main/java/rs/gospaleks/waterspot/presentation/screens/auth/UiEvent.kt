package rs.gospaleks.waterspot.presentation.screens.auth

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    object Error : UiEvent()
}