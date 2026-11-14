package nl.restpartijen.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.restpartijen.app.core.theme.RestpartijenTheme

@Composable
fun App() {
    RestpartijenTheme {
        // innerPadding keeps the content out from under the system bars.
        Scaffold { innerPadding ->
            // Temporary: in step 2.2 this text moves to strings.xml.
            Text(text = "Restpartijen", modifier = Modifier.padding(innerPadding))
        }
    }
}
