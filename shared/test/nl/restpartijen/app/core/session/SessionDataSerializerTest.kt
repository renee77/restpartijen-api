package nl.restpartijen.app.core.session

import androidx.datastore.core.CorruptionException
import kotlinx.coroutines.runBlocking
import nl.restpartijen.app.core.model.Role
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SessionDataSerializerTest {
    private val testDataSupplier = SessionData(
        token = "test-token",
        role = Role.SUPPLIER.toString(),
        supplierId = 1234L,
    )

    private val testDataCollector = SessionData(
        token = "test-token",
        role = Role.COLLECTOR.toString(),
    )

    private val testDataFakeRole = SessionData(
        token = "fake-test-token",
        role = "FAKE_ROLE",
    )

    @Test
    fun roundTripKeepsToken() {
        // Write the test data to a byte array output stream
        val output = ByteArrayOutputStream()
        // Use runBlocking to call the suspend function writeTo
        runBlocking {
            SessionDataSerializer.writeTo(testDataSupplier, output)
        }

        // Create an input stream from the output byte array
        val input = ByteArrayInputStream(output.toByteArray())
        // Use runBlocking to call the suspend function readFrom
        val result = runBlocking {
            SessionDataSerializer.readFrom(input)
        }

        assertEquals(testDataSupplier.token, result.token)
    }

    @Test
    fun roundTripKeepsRole() {
        val output = ByteArrayOutputStream()
        runBlocking {
            SessionDataSerializer.writeTo(testDataSupplier, output)
        }

        val input = ByteArrayInputStream(output.toByteArray())
        val result = runBlocking {
            SessionDataSerializer.readFrom(input)
        }

        assertEquals(testDataSupplier.role, result.role)
    }

    @Test
    fun roundTripKeepsNullSupplierIdAsNull() {
        val output = ByteArrayOutputStream()
        runBlocking {
            SessionDataSerializer.writeTo(testDataCollector, output)
        }

        val input = ByteArrayInputStream(output.toByteArray())
        val result = runBlocking {
            SessionDataSerializer.readFrom(input)
        }

        assertTrue(result.supplierId == null)
    }

    @Test
    fun emptyDefaultMeansNoSession() {
        val result = SessionDataSerializer.defaultValue
        assertTrue(result.token == null)
        assertTrue(result.role == null)
        assertTrue(result.supplierId == null)
    }

    @Test
    fun invalidBytesThrowCorruptionException() {
        val invalidBytes = "dit-is-geen-json".toByteArray()
        val input = ByteArrayInputStream(invalidBytes)

        assertFailsWith<CorruptionException> {
            runBlocking {
                SessionDataSerializer.readFrom(input)
            }
        }
    }

    @Test
    fun halfFilledRecordThrowsCorruptionException() {
        val incompleteJson = """{"role":null,"supplierId":1234}"""
        val input = ByteArrayInputStream(incompleteJson.toByteArray())

        assertFailsWith<CorruptionException> {
            runBlocking {
                SessionDataSerializer.readFrom(input)
            }
        }
    }

    @Test
    fun unknownRoleIsReadButGivesNoSession() {
        val output = ByteArrayOutputStream()
        runBlocking {
            SessionDataSerializer.writeTo(testDataFakeRole, output)
        }

        val input = ByteArrayInputStream(output.toByteArray())
        val result = runBlocking {
            SessionDataSerializer.readFrom(input)
        }

        assertTrue(result.role == "FAKE_ROLE")
        val session = result.toSessionOrNull()
        assertTrue(session == null)
    }
}
