package com.restpartijen.api.product.repository

import com.restpartijen.api.shared.Allergen
import org.jetbrains.exposed.v1.core.Table

/**
 * Table definition for the allergens of a surplus product (ERD §9.1, table PRODUCT_ALLERGENS).
 *
 * One row per allergen per product. A separate table, because a product has
 * zero to fourteen allergens (§9.8).
 */
object ProductAllergensTable : Table("product_allergens") {
    // The product this allergen belongs to; the database rejects an id that is not in ProductsTable.
    val productId = long("product_id").references(ProductsTable.id)

    // Stored by name (for example "MILK"); only the fourteen EU allergen groups fit.
    val allergen = enumerationByName<Allergen>("allergen", length = 30)

    // Composite key: a product lists each allergen at most once.
    override val primaryKey = PrimaryKey(productId, allergen)
}
