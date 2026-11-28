package nl.restpartijen.app.security.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.restpartijen.app.core.ui.ErrorMessage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import shared.generated.resources.Res
import shared.generated.resources.arrow_back
import shared.generated.resources.common_back
import shared.generated.resources.common_error
import shared.generated.resources.login_as_collector
import shared.generated.resources.login_as_supplier
import shared.generated.resources.login_temporary_note
import shared.generated.resources.login_title

/**
 * TEMPORARY test login screen: two buttons that each save a fixed session, so the rest of the app can
 * be tried out. Replaced by the real login screen (US-12, US-13).
 *
 * The navigation (S5) shows this screen and removes it again; this screen only calls [onLoggedIn] after the session
 * is saved, and [onBack] when the user goes back.
 */
@Composable
fun LoginScreen(onLoggedIn: () -> Unit, onBack: () -> Unit) {
    val viewModel = koinViewModel<LoginViewModel>()
    // Collects only while the screen is visible, so a screen in the background does no work.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Always the latest callback, without restarting the effect below when the caller passes a new lambda.
    val currentOnLoggedIn by rememberUpdatedState(onLoggedIn)

    // Runs when the state changes: once the session is saved, tell the navigation.
    LaunchedEffect(uiState) {
        if (uiState == LoginUiState.Done) currentOnLoggedIn()
    }

    LoginContent(
        uiState = uiState,
        onCollector = viewModel::loginAsCollector,
        onSupplier = viewModel::loginAsSupplier,
        onRetry = viewModel::retry,
        onBack = onBack,
    )
}

/** Draws [uiState]; remembers nothing itself, so a test or preview can pass in any state (state hoisting). */
@Composable
fun LoginContent(
    uiState: LoginUiState,
    onCollector: () -> Unit,
    onSupplier: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_back),
                    contentDescription = stringResource(Res.string.common_back),
                )
            }

            if (uiState == LoginUiState.Error) {
                ErrorMessage(message = stringResource(Res.string.common_error), onRetry = onRetry)
            } else {
                // Waiting, Saving and Done look the same, but the buttons only work while waiting. That blocks a
                // second tap in the screen too; the ViewModel ignores it anyway.
                val enabled = uiState == LoginUiState.Waiting
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(text = stringResource(Res.string.login_title), style = MaterialTheme.typography.headlineMedium)
                    Text(text = stringResource(Res.string.login_temporary_note))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onCollector, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(Res.string.login_as_collector))
                    }
                    Button(onClick = onSupplier, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                        Text(text = stringResource(Res.string.login_as_supplier))
                    }
                }
            }
        }
    }
}
