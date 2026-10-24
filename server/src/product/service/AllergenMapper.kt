package com.restpartijen.api.product.service

import com.restpartijen.api.shared.Allergen
import org.slf4j.LoggerFactory

/** Logs the tags that could not be mapped; named after the file, because there is no class. */
private val log = LoggerFactory.getLogger("com.restpartijen.api.product.service.AllergenMapper")

/**
 * Someone stated on Open Food Facts that the product has no allergens. A known tag, so no warning;
 * it still maps to no allergens, just like a product where nobody filled them in.
 */
private const val NO_ALLERGENS_TAG = "en:none"

/**
 * The longest tag the log shows. Real tags are short slugs; anything longer is cut off, so an odd answer
 * cannot flood the log (security review F-03, M-3).
 */
private const val MAX_LOGGED_TAG_LENGTH = 64

/** The most unknown tags one log line names, for the same reason as [MAX_LOGGED_TAG_LENGTH]. */
private const val MAX_LOGGED_TAGS = 5

/**
 * Control and format characters, and the Unicode line and paragraph separators: \p{Cntrl} alone covers only
 * ASCII, so NEL (U+0085) or U+2028 could still break a line in some log viewers (security review M-3).
 */
private val UNSAFE_IN_LOG = Regex("[\\p{Cc}\\p{Cf}\\p{Zl}\\p{Zp}]")

/**
 * The tag, cut off at [MAX_LOGGED_TAG_LENGTH] and with every [UNSAFE_IN_LOG] character replaced by "_".
 * The tag comes from Open Food Facts: without line breaks it cannot forge a log line (CWE-117).
 */
private fun String.forLog(): String = take(MAX_LOGGED_TAG_LENGTH).replace(UNSAFE_IN_LOG, "_")

/**
 * Tag ids of the fourteen EU allergens in the Open Food Facts taxonomy (§9.8), checked against
 * https://world.openfoodfacts.org/data/taxonomies/allergens.json on 22 Oct 2026.
 * A fixed table, because some names differ from ours, such as en:gluten for CEREALS_CONTAINING_GLUTEN.
 */
private val allergenByTag: Map<String, Allergen> = mapOf(
    "en:gluten" to Allergen.CEREALS_CONTAINING_GLUTEN,
    "en:crustaceans" to Allergen.CRUSTACEANS,
    "en:eggs" to Allergen.EGGS,
    "en:fish" to Allergen.FISH,
    "en:peanuts" to Allergen.PEANUTS,
    "en:soybeans" to Allergen.SOYBEANS,
    "en:milk" to Allergen.MILK,
    "en:nuts" to Allergen.NUTS,
    "en:celery" to Allergen.CELERY,
    "en:mustard" to Allergen.MUSTARD,
    "en:sesame-seeds" to Allergen.SESAME,
    "en:sulphur-dioxide-and-sulphites" to Allergen.SULPHITES,
    "en:lupin" to Allergen.LUPIN,
    "en:molluscs" to Allergen.MOLLUSCS,
)

/**
 * Translates the allergen tags of Open Food Facts into our [Allergen] values (§10.3).
 *
 * A tag outside the fourteen EU allergens, such as en:kiwi, is skipped, so one odd tag does not cost
 * the supplier the other allergens; one warning per call names how many and the first few.
 * en:none is skipped without a warning.
 *
 * @return each allergen once; empty when no tag is known, which does not prove the product has none.
 */
fun mapAllergenTags(tags: List<String>): Set<Allergen> {
    val unknown = mutableListOf<String>()
    val allergens = buildSet {
        for (tag in tags) {
            if (tag == NO_ALLERGENS_TAG) continue
            val allergen = allergenByTag[tag]
            if (allergen == null) unknown += tag else add(allergen)
        }
    }
    if (unknown.isNotEmpty()) {
        log.warn(
            "Skipped {} unknown allergen tags from Open Food Facts: {}",
            unknown.size,
            unknown.take(MAX_LOGGED_TAGS).joinToString(", ") { it.forLog() },
        )
    }
    return allergens
}
