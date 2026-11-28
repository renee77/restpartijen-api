package nl.restpartijen.app.core.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Guards the colour scheme of [RestpartijenTheme] (FTD §8.6, NFR-A7).
 *
 * A colour role that the theme leaves out falls back to the Material 3 default, which is purple. Material 3
 * components read more roles than the 12 from Lonneke's input, so these tests check that every role those
 * components use holds our own colour, and that every text pair stays readable.
 */
class RestpartijenColorsTest {
    @Test
    fun lightSchemeReplacesThePurpleDefaultsThatComponentsUse() {
        assertOwnColours(LightColors, lightColorScheme())
    }

    @Test
    fun darkSchemeReplacesThePurpleDefaultsThatComponentsUse() {
        assertOwnColours(DarkColors, darkColorScheme())
    }

    @Test
    fun lightSchemeTextReachesTheContrastForNormalText() {
        assertTextContrast(LightColors)
    }

    @Test
    fun darkSchemeTextReachesTheContrastForNormalText() {
        assertTextContrast(DarkColors)
    }

    @Test
    fun lightSchemeBordersReachTheContrastForUiParts() {
        assertBorderContrast(LightColors)
    }

    @Test
    fun darkSchemeBordersReachTheContrastForUiParts() {
        assertBorderContrast(DarkColors)
    }

    // Roles that Material 3 components read besides the 12 from Lonneke's input, for example the selected
    // filter chip and the navigation indicator (secondaryContainer), the navigation bar (surfaceContainer),
    // cards (surfaceContainerLow and surfaceContainerHighest), text field borders and labels (outline,
    // onSurfaceVariant) and dividers (outlineVariant).
    private fun rolesComponentsUse(scheme: ColorScheme): Map<String, Color> = mapOf(
        "primaryContainer" to scheme.primaryContainer,
        "onPrimaryContainer" to scheme.onPrimaryContainer,
        "secondaryContainer" to scheme.secondaryContainer,
        "onSecondaryContainer" to scheme.onSecondaryContainer,
        "tertiaryContainer" to scheme.tertiaryContainer,
        "onTertiaryContainer" to scheme.onTertiaryContainer,
        "surfaceVariant" to scheme.surfaceVariant,
        "onSurfaceVariant" to scheme.onSurfaceVariant,
        "outline" to scheme.outline,
        "outlineVariant" to scheme.outlineVariant,
        "surfaceContainerLow" to scheme.surfaceContainerLow,
        "surfaceContainer" to scheme.surfaceContainer,
        "surfaceContainerHigh" to scheme.surfaceContainerHigh,
        "surfaceContainerHighest" to scheme.surfaceContainerHighest,
        "inverseSurface" to scheme.inverseSurface,
        "inverseOnSurface" to scheme.inverseOnSurface,
    )

    private fun assertOwnColours(ours: ColorScheme, materialDefault: ColorScheme) {
        val defaults = rolesComponentsUse(materialDefault)
        rolesComponentsUse(ours).forEach { (role, colour) ->
            assertNotEquals(defaults.getValue(role), colour, "$role still has the Material 3 default colour")
        }
    }

    // Text colour on its background. WCAG 1.4.3 asks 4.5:1 for normal text.
    private fun assertTextContrast(scheme: ColorScheme) {
        val pairs =
            mapOf(
                "onPrimary on primary" to (scheme.onPrimary to scheme.primary),
                "onSecondary on secondary" to (scheme.onSecondary to scheme.secondary),
                "onTertiary on tertiary" to (scheme.onTertiary to scheme.tertiary),
                "onBackground on background" to (scheme.onBackground to scheme.background),
                "onSurface on surface" to (scheme.onSurface to scheme.surface),
                "onError on error" to (scheme.onError to scheme.error),
                "onPrimaryContainer on primaryContainer" to (scheme.onPrimaryContainer to scheme.primaryContainer),
                "onSecondaryContainer on secondaryContainer" to
                    (scheme.onSecondaryContainer to scheme.secondaryContainer),
                "onTertiaryContainer on tertiaryContainer" to
                    (scheme.onTertiaryContainer to scheme.tertiaryContainer),
                "onErrorContainer on errorContainer" to (scheme.onErrorContainer to scheme.errorContainer),
                "onSurfaceVariant on surfaceVariant" to (scheme.onSurfaceVariant to scheme.surfaceVariant),
                "onSurfaceVariant on surface" to (scheme.onSurfaceVariant to scheme.surface),
                "onSurface on surfaceContainerHighest" to (scheme.onSurface to scheme.surfaceContainerHighest),
                "inverseOnSurface on inverseSurface" to (scheme.inverseOnSurface to scheme.inverseSurface),
            )
        pairs.forEach { (name, pair) ->
            val ratio = contrast(pair.first, pair.second)
            assertTrue(ratio >= 4.5, "$name has contrast $ratio, needs at least 4.5")
        }
    }

    // The outline is the border of a text field. WCAG 1.4.11 asks 3:1 for parts of the UI.
    private fun assertBorderContrast(scheme: ColorScheme) {
        val ratio = contrast(scheme.outline, scheme.surface)
        assertTrue(ratio >= 3.0, "outline on surface has contrast $ratio, needs at least 3.0")
    }

    // The WCAG contrast ratio; luminance() is the relative luminance that WCAG uses.
    private fun contrast(first: Color, second: Color): Float {
        val lighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (lighter + 0.05f) / (darker + 0.05f)
    }
}
