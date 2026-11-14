package nl.restpartijen.app.core.session

import kotlinx.coroutines.flow.Flow
import nl.restpartijen.app.core.model.Session

/** Agreement K-A4: Lonneke builds the real, encrypted version (GB-3); everyone reads the session from here. */
interface SessionStore {
    /** The current session, or null when nobody is logged in. Emits again after login and logout. */
    val session: Flow<Session?>

    suspend fun save(session: Session)

    suspend fun clear()
}
