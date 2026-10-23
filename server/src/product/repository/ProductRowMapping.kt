package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.selectAll

// Shared by ExposedProductRepository and ExposedProductReader, so a row becomes a product in one place.
// Internal instead of private, so other files in this package can use them.
// Internal means the whole server module; GI-5, not the compiler, keeps other features out.

// The condition every query shares: a REMOVED product is invisible to the rest of the application.
internal fun notRemoved(): Op<Boolean> = ProductsTable.status neq ProductStatus.REMOVED

// The allergens of one product. They live in their own table, one row per allergen (§9.8).
// No dbQuery here: the caller already runs inside a transaction.
internal fun allergensOfProduct(productId: Long): Set<Allergen> =
    ProductAllergensTable
        .selectAll()
        .where { ProductAllergensTable.productId eq productId }
        .map { it[ProductAllergensTable.allergen] }
        .toSet()

// The one place that turns a row into the domain model, so every query builds a product the same way.
// The row must come from a join with SuppliersTable; the allergens live in another table, so the caller passes them.
internal fun ResultRow.toProduct(allergens: Set<Allergen>): SurplusProduct = SurplusProduct.create(
    category = this[ProductsTable.category],
    id = this[ProductsTable.id],
    name = this[ProductsTable.name],
    barcode = this[ProductsTable.barcode],
    originalPrice = Money.fromCents(this[ProductsTable.originalPriceCents]),
    supplier = SupplierSummary(
        id = this[SuppliersTable.id],
        name = this[SuppliersTable.name],
        latitude = this[SuppliersTable.latitude],
        longitude = this[SuppliersTable.longitude],
    ),
    bestBeforeAt = this[ProductsTable.bestBeforeAt],
    quantity = this[ProductsTable.quantity],
    status = this[ProductsTable.status],
    pickupWindow = PickupWindow(from = this[ProductsTable.pickupFrom], until = this[ProductsTable.pickupUntil]),
    allergens = allergens,
)