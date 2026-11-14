package nl.restpartijen.app.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.Font
import shared.generated.resources.Res
import shared.generated.resources.inter_bold
import shared.generated.resources.inter_regular
import shared.generated.resources.montserrat_bold
import shared.generated.resources.montserrat_regular

// Colours from Lonneke's theme input (14 Nov); every pair reaches at least 4.5:1 contrast (WCAG 1.4.3).
// Roles left out here keep the Material 3 default.
private val LightColors =
    lightColorScheme(
        primary = Color(0xFF3E762A),
        onPrimary = Color(0xFFFFFFFF),
        secondary = Color(0xFF2F5233),
        onSecondary = Color(0xFFFFFFFF),
        // Only as a surface with dark text on it: as text colour on white it is 2.55:1, unreadable.
        tertiary = Color(0xFFED8938),
        onTertiary = Color(0xFF303A32),
        background = Color(0xFFFAFAF6),
        onBackground = Color(0xFF303A32),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF303A32),
        error = Color(0xFFB3261E),
        onError = Color(0xFFFFFFFF),
    )

// Dark variants from the same input; every pair reaches at least 6.8:1.
private val DarkColors =
    darkColorScheme(
        primary = Color(0xFF8FC47A),
        onPrimary = Color(0xFF172316),
        secondary = Color(0xFFA7C98F),
        onSecondary = Color(0xFF172316),
        tertiary = Color(0xFFF2A65A),
        onTertiary = Color(0xFF172316),
        background = Color(0xFF121A14),
        onBackground = Color(0xFFE8F3D3),
        surface = Color(0xFF1D2A20),
        onSurface = Color(0xFFE8F3D3),
        error = Color(0xFFF28B82),
        onError = Color(0xFF172316),
    )

// Cards use medium, chips and menus small. Buttons ignore these: Material 3 gives them a fully rounded shape.
private val RestpartijenShapes =
    Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(8.dp),
    )

// Montserrat for headings, Inter for running text (Lonneke). Font() loads a resource, so this must be composable.
// Only Regular and Bold are bundled; other weights fall back to the nearest of the two.
@Composable
private fun restpartijenTypography(): Typography {
    val montserrat =
        FontFamily(
            Font(Res.font.montserrat_regular, FontWeight.Normal),
            Font(Res.font.montserrat_bold, FontWeight.Bold),
        )
    val inter =
        FontFamily(
            Font(Res.font.inter_regular, FontWeight.Normal),
            Font(Res.font.inter_bold, FontWeight.Bold),
        )
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = montserrat),
        displayMedium = default.displayMedium.copy(fontFamily = montserrat),
        displaySmall = default.displaySmall.copy(fontFamily = montserrat),
        headlineLarge = default.headlineLarge.copy(fontFamily = montserrat),
        headlineMedium = default.headlineMedium.copy(fontFamily = montserrat),
        headlineSmall = default.headlineSmall.copy(fontFamily = montserrat),
        titleLarge = default.titleLarge.copy(fontFamily = montserrat),
        titleMedium = default.titleMedium.copy(fontFamily = montserrat),
        titleSmall = default.titleSmall.copy(fontFamily = montserrat),
        bodyLarge = default.bodyLarge.copy(fontFamily = inter),
        bodyMedium = default.bodyMedium.copy(fontFamily = inter),
        bodySmall = default.bodySmall.copy(fontFamily = inter),
        labelLarge = default.labelLarge.copy(fontFamily = inter),
        labelMedium = default.labelMedium.copy(fontFamily = inter),
        labelSmall = default.labelSmall.copy(fontFamily = inter),
    )
}

/**
 * The app's look: every screen takes its colours, fonts and shapes from here, never its own.
 * Follows the device's dark mode unless [darkTheme] is given, for example in a preview.
 */
@Composable
fun RestpartijenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = restpartijenTypography(),
        shapes = RestpartijenShapes,
        content = content,
    )
}
