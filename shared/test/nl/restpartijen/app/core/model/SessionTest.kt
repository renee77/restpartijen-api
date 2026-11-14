package nl.restpartijen.app.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTest {
    @Test
    fun toStringHidesTheToken() {
        val session = Session(token = "secret-token", role = Role.SUPPLIER, supplierId = 7)

        assertEquals("Session(token=***, role=SUPPLIER, supplierId=7)", session.toString())
    }
}
