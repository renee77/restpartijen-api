package com.restpartijen.api.shared

/**
 * The fourteen allergen groups from Annex II of Regulation (EU) No 1169/2011 (§9.8).
 *
 * Modelled at group level: CEREALS_CONTAINING_GLUTEN and NUTS each cover several species.
 * A value outside this list cannot be deserialised, so the request fails with 400 (§10.2).
 */
enum class Allergen {
    CEREALS_CONTAINING_GLUTEN,
    CRUSTACEANS,
    EGGS,
    FISH,
    PEANUTS,
    SOYBEANS,
    MILK,
    NUTS,
    CELERY,
    MUSTARD,
    SESAME,
    SULPHITES,
    LUPIN,
    MOLLUSCS
}