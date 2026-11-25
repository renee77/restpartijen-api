package nl.restpartijen.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import nl.restpartijen.app.core.session.SessionStorageCheck

// Only starts the app: all screens live in the shared module, behind App().
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate, otherwise the status bar icons are white on white.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}
