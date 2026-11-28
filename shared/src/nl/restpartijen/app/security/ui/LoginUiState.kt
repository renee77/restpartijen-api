package nl.restpartijen.app.security.ui

sealed interface LoginUiState {
    data object Waiting : LoginUiState

    data object Saving : LoginUiState

    data object Done : LoginUiState

    data object Error : LoginUiState
}
