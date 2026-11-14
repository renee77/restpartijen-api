package nl.restpartijen.app.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.common_retry

/**
 * Shared error state (FTD §5.8): [message] with a retry button that calls [onRetry].
 * [message] is a text from strings.xml, never the server's message (FTD §11.2).
 */
@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = message)
        Button(onClick = onRetry) { Text(text = stringResource(Res.string.common_retry)) }
    }
}
