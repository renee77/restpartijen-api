package nl.restpartijen.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.restpartijen.app.core.theme.RestpartijenTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.app_name
import shared.generated.resources.arrow_back
import shared.generated.resources.common_back

@Composable
fun App() {
    RestpartijenTheme {
        // innerPadding keeps the content out from under the system bars.
        Scaffold { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text(text = stringResource(Res.string.app_name))
                // Temporary: shows that an icon from composeResources loads (step 2.4); replaced by navigation in block 4.
                Icon(
                    painter = painterResource(Res.drawable.arrow_back),
                    contentDescription = stringResource(Res.string.common_back),
                )
            }
        }
    }
}
