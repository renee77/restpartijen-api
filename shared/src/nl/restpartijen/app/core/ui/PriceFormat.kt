package nl.restpartijen.app.core.ui

/**
 * Formats an amount in whole euro cents as euros, using the notation of the given language.
 * The currency is always the euro, in every language (A-28).
 */
fun formatPrice(cents: Long, languageTag: String): String {
    val euros = cents / 100
    val restCents = (cents % 100).toString().padStart(2, '0')

    return if (languageTag.startsWith("en")) {
        "€$euros.$restCents"
    } else {
        "€ $euros,$restCents"
    }
}
