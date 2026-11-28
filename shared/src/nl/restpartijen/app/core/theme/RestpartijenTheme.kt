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
// The other roles come from the Material Color Utilities (the algorithm behind the Material Theme Builder),
// with Lonneke's primary, secondary and tertiary as source colours. Without them those roles keep the purple
// Material 3 default, which components such as chips, the navigation bar and cards use. RestpartijenColorsTest
// guards both points.
internal val LightColors =
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
        // Generated from Lonneke's colours, see above.
        primaryContainer = Color(0xFFB4F498),
        onPrimaryContainer = Color(0xFF1A5207),
        inversePrimary = Color(0xFF99D77E),
        secondaryContainer = Color(0xFFC4EDC3),
        onSecondaryContainer = Color(0xFF2B4E30),
        tertiaryContainer = Color(0xFFFFDCC6),
        onTertiaryContainer = Color(0xFF713700),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
        surfaceVariant = Color(0xFFDFE4D7),
        onSurfaceVariant = Color(0xFF43483F),
        inverseSurface = Color(0xFF2E312B),
        inverseOnSurface = Color(0xFFEFF2E8),
        outline = Color(0xFF73796E),
        outlineVariant = Color(0xFFC3C8BB),
        surfaceDim = Color(0xFFD8DBD1),
        surfaceBright = Color(0xFFF8FAF0),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF2F5EA),
        surfaceContainer = Color(0xFFECEFE5),
        surfaceContainerHigh = Color(0xFFE7E9DF),
        surfaceContainerHighest = Color(0xFFE1E4DA),
    )

// Dark variants from the same input; every pair reaches at least 6.8:1.
internal val DarkColors =
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
        // Generated from Lonneke's colours, like the light scheme.
        primaryContainer = Color(0xFF1A5207),
        onPrimaryContainer = Color(0xFFB4F498),
        inversePrimary = Color(0xFF336B20),
        secondaryContainer = Color(0xFF2B4E30),
        onSecondaryContainer = Color(0xFFC4EDC3),
        tertiaryContainer = Color(0xFF713700),
        onTertiaryContainer = Color(0xFFFFDCC6),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        surfaceVariant = Color(0xFF43483F),
        onSurfaceVariant = Color(0xFFC3C8BB),
        inverseSurface = Color(0xFFE1E4DA),
        inverseOnSurface = Color(0xFF2E312B),
        outline = Color(0xFF8D9387),
        outlineVariant = Color(0xFF43483F),
        surfaceDim = Color(0xFF11140F),
        surfaceBright = Color(0xFF373A33),
        surfaceContainerLowest = Color(0xFF0C0F0A),
        surfaceContainerLow = Color(0xFF191D17),
        surfaceContainer = Color(0xFF1D211A),
        surfaceContainerHigh = Color(0xFF272B25),
        surfaceContainerHighest = Color(0xFF32362F),
    )

// 8 dp corners from Lonneke's input. Text fields use extraSmall, chips and menus small, cards medium.
// Buttons ignore these (Material 3 makes them fully round), so PrimaryButton and SecondaryButton in core.ui set it.
private val RestpartijenShapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(8.dp),
    )

// Montserrat bold for headings and titles, as in Lonneke's design; Inter for running text (Lonneke).
// Font() loads a resource, so this must be composable.
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
        displayLarge = default.displayLarge.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        displayMedium = default.displayMedium.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        displaySmall = default.displaySmall.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        headlineLarge = default.headlineLarge.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        headlineMedium = default.headlineMedium.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        headlineSmall = default.headlineSmall.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        titleLarge = default.titleLarge.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        titleMedium = default.titleMedium.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
        titleSmall = default.titleSmall.copy(fontFamily = montserrat, fontWeight = FontWeight.Bold),
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
fun RestpartijenTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = restpartijenTypography(),
        shapes = RestpartijenShapes,
        content = content,
    )
}
