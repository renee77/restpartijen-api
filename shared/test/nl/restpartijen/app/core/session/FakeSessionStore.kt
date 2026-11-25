package nl.restpartijen.app.core.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import nl.restpartijen.app.core.model.Session

// In src/ until the encrypted version exists (A-13, #113), so others can build on it; then it moves to test/ (FTD §8.3).

/** Keeps the session in memory only, starting with [initial]; nothing survives a restart. */
class FakeSessionStore(initial: Session? = null) : SessionStore {
    private val current = MutableStateFlow(initial)

    override val session: Flow<Session?> = current.asStateFlow()

    override suspend fun save(session: Session) {
        current.value = session
    }

    override suspend fun clear() {
        current.value = null
    }
}
