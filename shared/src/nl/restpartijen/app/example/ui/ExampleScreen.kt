package nl.restpartijen.app.example.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.restpartijen.app.core.ui.ErrorMessage
import nl.restpartijen.app.core.ui.LoadingIndicator
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import shared.generated.resources.Res
import shared.generated.resources.common_error

/** Draws [uiState]; remembers nothing itself, so a test or preview can pass in any state (state hoisting). */
@Composable
fun ExampleScreen(
    uiState: ExampleUiState,
    onRetry: () -> Unit,
) {
    Scaffold { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (uiState) {
            ExampleUiState.Loading -> LoadingIndicator(modifier)
            ExampleUiState.Error -> ErrorMessage(stringResource(Res.string.common_error), onRetry, modifier)
            is ExampleUiState.Success ->
                LazyColumn(modifier) {
                    items(uiState.items) { item -> Text(text = item) }
                }
        }
    }
}

/** Connects [ExampleScreen] to its ViewModel from Koin. */
@Composable
fun ExampleRoute() {
    val viewModel = koinViewModel<ExampleViewModel>()
    // Collects only while the screen is visible, so a screen in the background does no work.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExampleScreen(uiState = uiState, onRetry = viewModel::load)
}
