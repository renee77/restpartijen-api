package com.restpartijen.api.shared

/**
 * The three product kinds (§9.3).
 *
 * Stored in the discriminator column `category` of SURPLUS_PRODUCTS.
 * Each kind has its own discount tiers (§9.4) and its own maximum plausible shelf life (§9.10).
 */
enum class ProductCategory {
    /** Chilled fresh products, such as dairy, meat and bread. */
    FRESH,

    /** Frozen products. */
    FROZEN,

    /** Shelf-stable products kept at room temperature. */
    AMBIENT,
}