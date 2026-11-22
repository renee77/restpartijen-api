package nl.restpartijen.app.core.ui

/**
 * Formats an amount in whole euro cents as euros, using the notation of the given language.
 * The currency is always the euro, in every language (A-28).
 */
fun formatPrice(cents: Long, languageTag: String): String {
    // Format the price in cents to a string with the euro sign and appropriate decimal separator based on the language tag.
    val euros = cents / 100
    val restCents = (cents % 100).toString().padStart(2, '0')

    if (euros > 999) {
        // Format euros with grouping for amounts above 999.
        val groupSeparator = if (languageTag.startsWith("en")) "," else "."
        val eurosWithGrouping = euros.toString().reversed().chunked(3).joinToString(groupSeparator).reversed()
        return when (languageTag.startsWith("en")) {
            true -> "€$eurosWithGrouping.$restCents"
            false -> "€ $eurosWithGrouping,$restCents"
        }
    }
    return when (languageTag.startsWith("en")) {
        true -> "€$euros.$restCents"
        false -> "€ $euros,$restCents"
    }
}
