package nl.restpartijen.app.core.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import nl.restpartijen.app.core.theme.Spacing
import nl.restpartijen.app.core.ui.AppLogo
import nl.restpartijen.app.core.ui.PrimaryButton
import nl.restpartijen.app.core.ui.SecondaryButton
import org.jetbrains.compose.resources.stringResource
import shared.generated.resources.Res
import shared.generated.resources.permission_ask_again
import shared.generated.resources.permission_network_title
import shared.generated.resources.permission_network_where
import shared.generated.resources.permission_network_why
import shared.generated.resources.permission_not_now

/**
 * Explains why the app needs the local network permission (FTD §15). Shown instead of the app while the
 * permission is not granted and the user has not chosen "Niet nu".
 *
 * The screen holds no state and asks nothing itself: the caller decides what each button does.
 *
 * @param canAskAgain true: Android will still show its permission dialog, so the screen offers "Opnieuw vragen".
 *   false: Android no longer shows the dialog, so the screen says where to turn the permission on in the settings.
 * @param onAskAgain called when the user taps "Opnieuw vragen". That button appears only when [canAskAgain] is true.
 * @param onNotNow called when the user taps "Niet nu". That button always appears; the app then continues
 *   without the permission.
 * @param modifier applied to the whole screen.
 */
@Composable
fun PermissionExplanationScreen(
    canAskAgain: Boolean,
    onAskAgain: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            // The app draws edge-to-edge (MainActivity) and this screen has no Scaffold, so it keeps its content
            // clear of the system bars itself. The Surface stays full screen, so the background runs behind them.
            // The content scrolls, because in landscape it does not fit and the buttons would be unreachable:
            // the user could then not get into the app (FTD §15).
            modifier = Modifier
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium),
        ) {
            AppLogo()
            Text(
                text = stringResource(Res.string.permission_network_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                text = stringResource(Res.string.permission_network_why),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            if (!canAskAgain) {
                Text(
                    text = stringResource(Res.string.permission_network_where),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                if (canAskAgain) {
                    PrimaryButton(
                        text = stringResource(Res.string.permission_ask_again),
                        onClick = onAskAgain,
                        modifier = Modifier.weight(1f),
                    )
                }
                SecondaryButton(
                    text = stringResource(Res.string.permission_not_now),
                    onClick = onNotNow,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
