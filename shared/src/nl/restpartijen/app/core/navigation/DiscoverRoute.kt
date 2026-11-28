package nl.restpartijen.app.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Opens the Discover screen: the start of the Discover tab, with all products (FTD §8.4). The tab bar puts it at the
 * bottom of the back stack. It has no arguments: the search text is state of the screen, not of the route. Tapping a
 * product puts a [ProductDetailRoute] on top (K-A1).
 */
@Serializable // Lets Navigation 3 save the back stack, so it survives rotation.
data object DiscoverRoute : NavKey {
}
