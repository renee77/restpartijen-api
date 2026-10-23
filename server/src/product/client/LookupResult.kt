package com.restpartijen.api.product.client

/**
 * The outcome of one call to Open Food Facts (§10.3).
 * Each variant leads to its own handling in the service, so the service has to handle all three.
 */
sealed interface LookupResult {
    /**
     * Open Food Facts knows the product.
     *
     * @property name the product name, or null when Open Food Facts has none.
     * @property allergenTags the tags exactly as Open Food Facts sends them, such as "en:milk";
     * an empty list does not prove that the product has no allergens.
     */
    data class Found(val name: String?, val allergenTags: List<String>) : LookupResult

    /** Open Food Facts answered 404: it does not know the barcode. */
    data object NotFound : LookupResult

    /** Open Food Facts could not give a usable answer: time-out, no connection, an error status or a broken body. */
    data object Unavailable : LookupResult
}
