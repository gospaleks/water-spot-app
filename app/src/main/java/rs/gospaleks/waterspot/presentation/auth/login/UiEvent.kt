package rs.gospaleks.waterspot.presentation.auth.login

sealed class UiEvent {
    object NavigateToHome : UiEvent()
    object Error : UiEvent()
}
