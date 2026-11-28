package nl.restpartijen.app.core.session

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemorySessionStoreTest {
    @Test
    fun startsWithoutSession() = runBlocking {
        val store = MemorySessionStore()

        assertNull(store.session.first())
    }

    @Test
    fun saveAndClearChangeTheSession() = runBlocking {
        val store = MemorySessionStore()
        val session = Session("token", Role.COLLECTOR, null)

        store.save(session)
        assertEquals(session, store.session.first())

        store.clear()
        assertNull(store.session.first())
    }

    @Test
    fun twoStoresDoNotShareASession() = runBlocking {
        val first = MemorySessionStore()
        val second = MemorySessionStore()

        first.save(Session("token", Role.SUPPLIER, 7))

        assertNull(second.session.first())
    }
}
