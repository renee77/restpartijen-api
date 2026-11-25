package nl.restpartijen.app.core.session

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
import java.io.InputStream
import java.io.OutputStream

/**
 * What the session file holds: the fields of a [Session] in a form that survives a round trip through JSON.
 * The role is stored by name, so a value that no longer exists in [Role] is recognised as damage and does not
 * crash the app. All fields are null when nobody is logged in; that is also what a new file starts with.
 */
internal object SessionDataSerializer : Serializer<SessionData> {

    override val defaultValue: SessionData = SessionData()

    override suspend fun readFrom(input: InputStream): SessionData {
        val jsonString = input.readBytes().toString(Charsets.UTF_8)

        return try {
            val data = Json.decodeFromString(SessionData.serializer(), jsonString)

            val isEmpty = data.token == null &&
                data.role == null &&
                data.supplierId == null

            val isComplete = data.token != null &&
                data.role != null &&
                Role.entries.any { it.name == data.role }

            if (!isEmpty && !isComplete) {
                throw CorruptionException("Incomplete or invalid session data")
            }

            data
        } catch (e: SerializationException) {
            throw CorruptionException("Cannot read session data", e)
        }
    }

    override suspend fun writeTo(t: SessionData, output: OutputStream) {
        val json = Json.encodeToString(t)
        output.write(json.toByteArray())
    }
}
