package nl.restpartijen.app.core.ui


/**
 * Formats an amount in whole euro cents as euros, using the notation of the given language.
 * The currency is always the euro, in every language (A-28). Without a language,
 * the language of the device is used.
 */
fun formatPrice(cents: Long, languageTag: String): String {
    // Format the price in cents to a string with the euro sign and appropriate decimal separator based on the language tag.
    val euros = cents / 100
    val restCents = (cents % 100).toString().padStart(2, '0')

    if (euros > 999) {
        // Format euros with grouping for amounts above 999.
        val groupSeparator = if (languageTag == "en") "," else "."
        val eurosWithGrouping = euros.toString().reversed().chunked(3).joinToString(groupSeparator).reversed()
        return when (languageTag) {
            "en" -> "€$eurosWithGrouping.$restCents"
            else -> "€ $eurosWithGrouping,$restCents"
        }
    }
    return when (languageTag) {
        "en" -> "€$euros.$restCents"
        else -> "€ $euros,$restCents"
    }
}
