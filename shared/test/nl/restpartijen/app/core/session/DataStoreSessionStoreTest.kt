package nl.restpartijen.app.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataStoreSessionStoreTest {
    private fun createTestDataStore(): DataStore<SessionData> {
        val file = File.createTempFile("session-test", ".json")
        file.delete()

        return DataStoreFactory.create(
            serializer = SessionDataSerializer,
            produceFile = { file },
        )
    }

    @Test
    fun saveMakesSessionEmitThatSession() = runBlocking {
        val store = DataStoreSessionStore(createTestDataStore())
        val session = Session("token", Role.COLLECTOR, null)
        store.save(session)
        assertEquals(session, store.session.first())
    }

    @Test
    fun clearMakesSessionEmitNull() = runBlocking {
        val store = DataStoreSessionStore(FakeDataStore(SessionData()))
        store.save(Session("token", Role.SUPPLIER, null))
        store.clear()
        assertNull(store.session.first())
    }

    @Test
    fun secondSaveReplacesTheFirst() = runBlocking {
        val store = DataStoreSessionStore(FakeDataStore(SessionData()))
        val session1 = Session("token1", Role.SUPPLIER, null)
        val session2 = Session("token2", Role.COLLECTOR, null)
        store.save(session1)
        store.save(session2)
        assertEquals(session2, store.session.first())
    }

    @Test
    fun emptyStoreEmitsNull() = runBlocking {
        val store = DataStoreSessionStore(createTestDataStore())
        assertNull(store.session.first())
    }
}
