package nl.restpartijen.app.core.session

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Turns [SessionData] into bytes and back, for the DataStore. It does not encrypt: the AeadSerializer of
 * datastore-tink wraps it (A-13). Anything that cannot be read, such as invalid JSON or a half-filled record,
 * is reported as a [CorruptionException]. The DataStore then starts again with an empty session instead of
 * crashing the app (FTD §16.3). A role that no longer exists is not checked here: `toSessionOrNull()` gives
 * no session for it.
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
                data.role != null

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
