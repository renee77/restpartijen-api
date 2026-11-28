package nl.restpartijen.app.core.session

import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionDataTest {
    @Test
    fun emptyRecordGivesNoSession() {
        assertNull(SessionData().toSessionOrNull())
    }

    @Test
    fun recordWithTokenButWithoutRoleGivesNoSession() {
        assertNull(SessionData(token = "token").toSessionOrNull())
    }

    @Test
    fun recordWithUnknownRoleGivesNoSession() {
        assertNull(SessionData(token = "token", role = "FAKE_ROLE").toSessionOrNull())
    }

    @Test
    fun fullRecordGivesTheSameSession() {
        val session = Session("token", Role.SUPPLIER, 7)

        assertEquals(session, session.toData().toSessionOrNull())
    }

    @Test
    fun nullSupplierIdStaysNull() {
        val result = Session("token", Role.COLLECTOR, null).toData().toSessionOrNull()

        assertNotNull(result)
        assertNull(result.supplierId)
    }
}
