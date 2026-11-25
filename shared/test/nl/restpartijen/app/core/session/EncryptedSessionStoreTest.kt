package nl.restpartijen.app.core.session

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import kotlin.test.Test
import kotlin.test.assertEquals

class EncryptedSessionStoreTest {
    private val session = Session("token", Role.SUPPLIER, 7)

    @Test
    fun usesTheEncryptedStoreWhenKeystoreIsUsed() = runBlocking {
        val dataStore = FakeDataStore(SessionData())
        val store = EncryptedSessionStore.create(
            keystoreIsUsed = true,
            encryptedDataStore = { DataStoreSessionStore(dataStore) },
        )

        store.save(session)

        assertEquals(session.toData(), dataStore.data.first())
        assertEquals(session, store.session.first())
    }

    @Test
    fun usesTheMemoryStoreWhenKeystoreIsNotUsed() = runBlocking {
        val store = EncryptedSessionStore.create(
            keystoreIsUsed = false,
            encryptedDataStore = { DataStoreSessionStore(FakeDataStore(SessionData())) },
        )

        store.save(session)

        assertEquals(session, store.session.first())
    }

    @Test
    fun neverBuildsTheEncryptedStoreWithoutKeystore() = runBlocking {
        var builds = 0
        val store = EncryptedSessionStore.create(
            keystoreIsUsed = false,
            encryptedDataStore = {
                builds++
                DataStoreSessionStore(FakeDataStore(SessionData()))
            },
        )

        store.save(session)
        store.clear()
        store.session.first()

        assertEquals(0, builds)
    }

    @Test
    fun writesNothingToTheEncryptedDataStoreWithoutKeystore() = runBlocking {
        val dataStore = FakeDataStore(SessionData())
        val store = EncryptedSessionStore.create(
            keystoreIsUsed = false,
            encryptedDataStore = { DataStoreSessionStore(dataStore) },
        )

        store.save(session)

        assertEquals(SessionData(), dataStore.data.first())
    }
}
