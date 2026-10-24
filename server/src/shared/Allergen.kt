package com.restpartijen.api.shared

/**
 * The fourteen allergen groups from Annex II of Regulation (EU) No 1169/2011 (§9.8).
 *
 * Modelled at group level: CEREALS_CONTAINING_GLUTEN and NUTS each cover several species.
 * A value outside this list cannot be deserialised, so the request fails with 400 (§10.2).
 */
enum class Allergen {
    /** Cereals containing gluten, such as wheat, rye, barley and oats. */
    CEREALS_CONTAINING_GLUTEN,

    /** Crustaceans, such as shrimp and crab. */
    CRUSTACEANS,

    /** Eggs and products made from them. */
    EGGS,

    /** Fish and products made from it. */
    FISH,

    /** Peanuts and products made from them. */
    PEANUTS,

    /** Soybeans and products made from them. */
    SOYBEANS,

    /** Milk and products made from it, lactose included. */
    MILK,

    /** Tree nuts, such as almonds, hazelnuts and walnuts; peanuts are a group of their own. */
    NUTS,

    /** Celery and products made from it. */
    CELERY,

    /** Mustard and products made from it. */
    MUSTARD,

    /** Sesame seeds and products made from them. */
    SESAME,

    /** Sulphur dioxide and sulphites, above 10 mg per kilogram or litre. */
    SULPHITES,

    /** Lupin and products made from it. */
    LUPIN,

    /** Molluscs, such as mussels and squid. */
    MOLLUSCS,
}
