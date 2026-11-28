package nl.restpartijen.app

import androidx.compose.runtime.Composable
import nl.restpartijen.app.core.theme.RestpartijenTheme

/**
 * @param permissionGate wraps the app content, so a platform can hold it back
 * until a permission is granted. By default the content shows directly.
 */
@Composable
fun App(permissionGate: @Composable (content: @Composable () -> Unit) -> Unit = { content -> content() }) {
    RestpartijenTheme {
        permissionGate { AppNavigation() }
    }
}
