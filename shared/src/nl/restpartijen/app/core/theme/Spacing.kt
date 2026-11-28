package nl.restpartijen.app.core.theme

import androidx.compose.ui.unit.dp

/**
 * Fixed spacing for every screen, so all screens use the same sizes (FTD §8.6).
 *
 * Measured in Lonneke's design ("Restpartijen-app thema in beeld", 28 Nov): [small] inside a group, [medium]
 * between parts and as the side margin of lists, [large] as the side margin of forms such as login.
 * A plain object and not part of MaterialTheme, because spacing does not differ between light and dark.
 */
object Spacing {
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
}
