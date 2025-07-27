package rs.gospaleks.waterspot.presentation.screens.auth.login

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    object Error : UiEvent()
}
