package nl.restpartijen.app.core.session

import kotlinx.serialization.Serializable
import nl.restpartijen.app.core.model.Role
import nl.restpartijen.app.core.model.Session
/**
 * What the session file holds: the fields of a [Session] in a form that survives a round trip through JSON.
 * The role is stored by name, so a value that no longer exists in [Role] is recognised as damage and does not
 * crash the app. All fields are null when nobody is logged in; that is also what a new file starts with.
 */
@Serializable
internal data class SessionData(val token: String? = null, val role: String? = null, val supplierId: Long? = null)

/** Converts a session to the form that is stored. */
internal fun Session.toData(): SessionData = SessionData(token, role.name, supplierId)

/** Returns the session that this record holds, or null when the record is empty. */
internal fun SessionData.toSessionOrNull(): Session? {
    val storedToken = token ?: return null
    val storedRole = Role.entries.firstOrNull { it.name == role } ?: return null
    return Session(storedToken, storedRole, supplierId)
}
