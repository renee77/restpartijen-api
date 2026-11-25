package nl.restpartijen.app.core.session

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.restpartijen.app.core.model.Session

/**
 * A [SessionStore] on top of a DataStore. It does not know whether the DataStore is encrypted: the caller
 * decides that, so the logic can be tested on the JVM with a plain file. [session] emits again after every
 * [save] and [clear].
 */
internal class DataStoreSessionStore(private val dataStore: DataStore<SessionData>) : SessionStore {
    override val session: Flow<Session?>
        get() = dataStore.data.map { it.toSessionOrNull() }

    override suspend fun save(session: Session) {
        dataStore.updateData {
            session.toData()
        }
    }

    override suspend fun clear() {
        dataStore.updateData { SessionData() }
    }
}
