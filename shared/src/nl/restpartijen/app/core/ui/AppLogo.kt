package nl.restpartijen.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import shared.generated.resources.Res
import shared.generated.resources.logo

// The size from Lonneke's design (login screen): about 52 x 90 dp. logo.xml itself is 90 x 156 dp,
// which is too big next to the text, so every screen uses this composable instead of the bare resource.
private val LogoWidth = 52.dp
private val LogoHeight = 90.dp

/**
 * The app's logo (the carrot), at the same size on every screen, such as login and the permission screen.
 * Decorative: the screens that show it also show a title, so a screen reader skips it (NFR-A7).
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = null,
        modifier = modifier.size(width = LogoWidth, height = LogoHeight),
    )
}
