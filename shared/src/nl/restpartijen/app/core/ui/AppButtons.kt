package nl.restpartijen.app.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Material 3 makes buttons fully round and 40 dp high, and the theme cannot change that. Lonneke's design has
// 8 dp corners and higher buttons, so every screen uses these two instead of Button and OutlinedButton.
// 48 dp is also the minimum touch target of NFR-A7; the design measures about 50 dp.
private val ButtonMinHeight = 48.dp

/** The main action of a screen, such as "Inloggen": filled with the primary colour. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonMinHeight),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(text = text)
    }
}

/** A second choice next to the main action, such as "Niet nu": a dark green border and text, as in the design. */
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ButtonMinHeight),
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
    ) {
        Text(text = text)
    }
}
