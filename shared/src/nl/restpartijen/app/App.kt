package nl.restpartijen.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.restpartijen.app.core.theme.RestpartijenTheme
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.app_name

@Composable
fun App() {
    RestpartijenTheme {
        // innerPadding keeps the content out from under the system bars.
        Scaffold { innerPadding ->
            Text(text = stringResource(Res.string.app_name), modifier = Modifier.padding(innerPadding))
        }
    }
}
