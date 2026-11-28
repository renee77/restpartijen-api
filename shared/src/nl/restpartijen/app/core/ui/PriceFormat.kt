package nl.restpartijen.app.core.ui

/**
 * Formats an amount in whole euro cents as euros, using the notation of the given language.
 * The currency is always the euro, in every language (A-28).
 */
fun formatPrice(cents: Long, languageTag: String): String {
    val euros = cents / 100
    val restCents = (cents % 100).toString().padStart(2, '0')
    val isEnglish = languageTag.startsWith("en")
    val groupSeparator = if (isEnglish) "," else "."
    val eurosWithGrouping = euros.toString().reversed().chunked(3).joinToString(groupSeparator).reversed()

    return if (isEnglish) {
        "€$eurosWithGrouping.$restCents"
    } else {
        "€ $eurosWithGrouping,$restCents"
    }
}
