package nl.restpartijen.app

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import nl.restpartijen.app.core.navigation.DiscoverRoute
import nl.restpartijen.app.core.navigation.ProductDetailRoute

/** Connects every route to its screen (A-23): the only place that knows all features. */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(DiscoverRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<DiscoverRoute> { route ->
                TemporaryDiscoverScreen { productId -> backStack.add(ProductDetailRoute(productId)) }
            }

            entry<ProductDetailRoute> { route ->
                TemporaryProductDetailScreen(
                    productId = route.productId,
                    onNextProduct = { backStack.add(ProductDetailRoute(route.productId + 1)) },
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
