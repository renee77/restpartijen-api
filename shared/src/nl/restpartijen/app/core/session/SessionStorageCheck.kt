package nl.restpartijen.app.core.session

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session

// TEMPORARY: remove before committing. Checks encryptedSessionStore on the emulator (GB-3, L1).

// One store for the whole process: two DataStores on the same file make the second one fail.
private var store: SessionStore? = null

private fun storeFor(context: Context): SessionStore =
    store ?: encryptedSessionStore(context.applicationContext).also { store = it }

@Composable
fun SessionStorageCheck() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var shown by remember { mutableStateOf("Nothing done yet.") }

    fun run(action: suspend () -> String) {
        scope.launch {
            shown = try {
                action()
            } catch (e: Exception) {
                "Error: ${e::class.simpleName}"
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Button(onClick = {
            run {
                storeFor(context).save(Session("test-token-123", Role.SUPPLIER, 7))
                "Saved."
            }
        }) { Text("Opslaan") }
        Button(onClick = {
            run {
                storeFor(context).clear()
                "Cleared."
            }
        }) { Text("Wissen") }
        Button(onClick = { run { "Session: ${storeFor(context).session.first()}" } }) { Text("Tonen") }
        Text(shown)
    }
}
