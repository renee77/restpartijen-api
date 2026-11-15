package nl.restpartijen.app.core.ui

fun formatPrice(
    cents: Long,
    languageTag: String
): String {
    // Format the price in cents to a string with the euro sign and appropriate decimal separator based on the language tag.
    val euros = cents / 100
    val restCents = (cents % 100).toString().padStart(2, '0')
    // Return the formatted price string based on the language tag.
    return when (languageTag) {
        "en" -> "€$euros.$restCents"
        else -> "€ $euros,$restCents"
    }
}
