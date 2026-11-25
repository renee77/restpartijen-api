package nl.restpartijen.app.core.model

/** A logged-in user, from the API's LoginResponse. [supplierId] is only set for a supplier. */
data class Session(val token: String, val role: Role, val supplierId: Long? = null) {
    /** Shows the token as `***`, so logging a session never leaks it (same rule as the server, §16.7). */
    override fun toString(): String = "Session(token=***, role=$role, supplierId=$supplierId)"
}
