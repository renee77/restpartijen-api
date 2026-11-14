package nl.restpartijen.app.core.model

/** A reservation as the product detail screen shows it after reserving (FTD §5.4); more fields come when a screen needs them. */
data class Reservation(
    val id: Long,
    val productId: Long,
    // Cents in a Long, like B-13 on the server: a Double cannot store amounts like 0.10 exactly (decision point 8).
    val reservedPriceInCents: Long,
    val expiresAt: String,
)
