package nl.restpartijen.app

import androidx.compose.runtime.Composable
import nl.restpartijen.app.core.theme.RestpartijenTheme
import nl.restpartijen.app.example.ui.ExampleRoute

@Composable
fun App() {
    RestpartijenTheme {
        // Walking skeleton: the example screen until navigation arrives in block 4.
        ExampleRoute()
    }
}
