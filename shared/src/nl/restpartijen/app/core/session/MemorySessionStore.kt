package nl.restpartijen.app.core.session

import kotlinx.coroutines.flow.MutableStateFlow
import nl.restpartijen.app.core.model.Session

/**
 * A [SessionStore] that keeps the session in memory only. It is used when the Android Keystore cannot protect
 * the key, so that no token is written to the device unencrypted (O-7, FTD §16.3). The user is logged out
 * when the app restarts.
 */
internal class MemorySessionStore : SessionStore {

    override val session = MutableStateFlow<Session?>(null)

    override suspend fun save(session: Session) {
        // TODO: Convert Session to SessionData
    }

    override suspend fun clear() {
        // TODO: Return an empty SessionData
    }
}
