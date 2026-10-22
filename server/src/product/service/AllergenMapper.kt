package com.restpartijen.api.product.service

import com.restpartijen.api.shared.Allergen
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("com.restpartijen.api.product.service.AllergenMapper")

// Someone stated on Open Food Facts that the product has no allergens. A known tag, so no warning;
// it still maps to no allergens, just like a product where nobody filled them in.
private const val NO_ALLERGENS_TAG = "en:none"

// Tag ids of the fourteen EU allergens in the Open Food Facts taxonomy (§9.8), checked against
// https://world.openfoodfacts.org/data/taxonomies/allergens.json on 22 Oct 2026.
// A fixed table, because some names differ from ours, such as en:gluten for CEREALS_CONTAINING_GLUTEN.
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
 * A tag outside the fourteen EU allergens, such as en:kiwi, is logged and skipped, so one odd tag
 * does not cost the supplier the other allergens. en:none is skipped without a warning.
 *
 * @return each allergen once; empty when no tag is known, which does not prove the product has none.
 */
fun mapAllergenTags(tags: List<String>): Set<Allergen> = buildSet {
    for (tag in tags) {
        if (tag == NO_ALLERGENS_TAG) continue
        val allergen = allergenByTag[tag]
        if (allergen == null) {
            log.warn("Skipped unknown allergen tag from Open Food Facts: {}", tag)
        } else {
            add(allergen)
        }
    }
}
