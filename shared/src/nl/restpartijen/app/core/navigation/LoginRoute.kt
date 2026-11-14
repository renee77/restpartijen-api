package nl.restpartijen.app.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Opens the login screen on top of the current one (K-A3). After a successful login the navigation removes it again,
 * so the user is back where they were and that screen retries its action (FTD §5.4).
 */
@Serializable // Lets Navigation 3 save the back stack, so it survives rotation.
data object LoginRoute : NavKey
