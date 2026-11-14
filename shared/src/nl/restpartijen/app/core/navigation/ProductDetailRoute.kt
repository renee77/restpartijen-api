package nl.restpartijen.app.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Opens the product detail screen (K-A1): Discover puts it on the back stack, the detail screen reads [productId]
 * and loads the product itself, so it always shows the current price.
 */
@Serializable // Lets Navigation 3 save the back stack, so it survives rotation.
data class ProductDetailRoute(val productId: Long) : NavKey
