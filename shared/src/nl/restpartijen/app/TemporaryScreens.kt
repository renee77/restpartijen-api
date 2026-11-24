package nl.restpartijen.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Temporary (GB-2, S1): screens to check the navigation and the two decorators by hand.
// They are replaced by the real screens in S2 (tabs) and S6 (product detail).

/** Counts in a ViewModel, so you can see which screen the ViewModel belongs to. */
class CounterViewModel : ViewModel() {
    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.update { it + 1 }
    }
}

@Composable
fun TemporaryDiscoverScreen(onProductClick: (Long) -> Unit) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "Ontdekken")
        Button(onClick = { onProductClick(42) }) {
            Text("Product 42")
        }
    }
}

/**
 * Shows [productId] and two counters: one in a [CounterViewModel] and one in `rememberSaveable`. Fetches its own
 * ViewModel, against the usual split into a stateless screen and a connecting composable: this screen is only a check.
 */
@Composable
fun TemporaryProductDetailScreen(productId: Long, onNextProduct: () -> Unit, onBack: () -> Unit) {
    val viewModel = viewModel { CounterViewModel() }
    val viewModelCount by viewModel.count.collectAsStateWithLifecycle()
    var savedCount by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Product $productId")
        Text("ViewModel: $viewModelCount")
        Text("rememberSaveable: $savedCount")
        Button(onClick = {
            viewModel.increment()
            savedCount++
        }) { Text("+1") }
        Button(onClick = onNextProduct) { Text("Volgend product") }
        Button(onClick = onBack) { Text("Terug") }
    }
}
