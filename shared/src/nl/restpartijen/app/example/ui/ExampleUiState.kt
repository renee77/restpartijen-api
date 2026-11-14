package nl.restpartijen.app.example.ui

/** What the example screen shows: always exactly one of these, so the screen cannot show contradicting states. */
sealed interface ExampleUiState {
    data object Loading : ExampleUiState

    data class Success(val items: List<String>) : ExampleUiState

    // No text: the screen picks one from strings.xml, so the message works in both languages (FTD §11.2).
    data object Error : ExampleUiState
}
