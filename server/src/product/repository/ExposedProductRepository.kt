package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.SupplierSummary
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

/**
 * Products in the database. Deleting is a soft delete: the row stays and gets status REMOVED (§9.5, B-19).
 * A REMOVED product counts as not found, as the Repository contract promises.
 */
class ExposedProductRepository(private val clock: Clock) : ProductRepository {
    override suspend fun findById(id: Long): SurplusProduct? = dbQuery {
        // let gives the row a name (it), so it can deliver both the id for the allergens and the product itself.
        (ProductsTable innerJoin SuppliersTable).selectAll().where { (ProductsTable.id eq id) and notRemoved() }
            .singleOrNull()?.let { it.toProduct(allergensOfProduct(it[ProductsTable.id])) }
    }

    override suspend fun findAll(): List<SurplusProduct> = dbQuery {
        // Ordered by id, so products come back in the order they were created.
        (ProductsTable innerJoin SuppliersTable).selectAll().where { notRemoved() }.orderBy(ProductsTable.id)
            .map { it.toProduct(allergensOfProduct(it[ProductsTable.id])) }
    }

    override suspend fun create(item: SurplusProduct): SurplusProduct = dbQuery {
        // No id and no status: the database chooses the id, and status defaults to LISTED.
        val newId = ProductsTable.insert {
            it[ProductsTable.supplierId] = item.supplier.id
            it[ProductsTable.createdAt] = clock.now()
            writeChangeableColumns(it, item)
        }[ProductsTable.id]

        // After the insert, because the allergen rows need the new id.
        insertAllergensOfProduct(newId, item.allergens)

        // Read back: SurplusProduct has no copy(), and this returns exactly what was stored.
        // The !! is safe: the product was stored a moment ago in this same transaction.
        findById(newId)!!
    }

    override suspend fun update(item: SurplusProduct): Boolean = dbQuery {
        // update() returns how many rows changed: 0 means no product with this id, or it was REMOVED.
        // Supplier, created_at and status stay as they are; status changes go through ProductStatusUpdater.
        val updated = ProductsTable.update({ (ProductsTable.id eq item.id) and notRemoved() }) {
            writeChangeableColumns(it, item)
        } > 0
        // Only for an existing, non-removed product. Replace the whole set:
        // simpler than working out which allergens were added or removed.
        if (updated) {
            ProductAllergensTable.deleteWhere { productId eq item.id }
            insertAllergensOfProduct(item.id, item.allergens)
        }
        updated
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        // Soft delete: only the status changes. A second delete finds no row that is not REMOVED.
        ProductsTable.update({ (ProductsTable.id eq id) and notRemoved() }) {
            it[ProductsTable.status] = ProductStatus.REMOVED
        } > 0
    }
}

// The condition every query shares: a REMOVED product is invisible to the rest of the application.
private fun notRemoved(): Op<Boolean> = ProductsTable.status neq ProductStatus.REMOVED

// The columns that both create and update write. Supplier, created_at and status are left out:
// update must not change them, and status changes go through ProductStatusUpdater.
// UpdateBuilder is the common parent of Exposed's insert and update statements, so one function serves both.
private fun writeChangeableColumns(row: UpdateBuilder<*>, item: SurplusProduct) {
    row[ProductsTable.category] = item.category
    row[ProductsTable.name] = item.name
    row[ProductsTable.barcode] = item.barcode
    row[ProductsTable.originalPriceCents] = item.originalPrice.cents
    row[ProductsTable.bestBeforeAt] = item.bestBeforeAt
    row[ProductsTable.quantity] = item.quantity
    row[ProductsTable.pickupFrom] = item.pickupWindow.from
    row[ProductsTable.pickupUntil] = item.pickupWindow.until
}

// The allergens of one product. They live in their own table, one row per allergen (§9.8).
// No dbQuery here: the caller already runs inside a transaction.
private fun allergensOfProduct(productId: Long): Set<Allergen> =
    ProductAllergensTable
        .selectAll()
        .where { ProductAllergensTable.productId eq productId }
        .map { it[ProductAllergensTable.allergen] }
        .toSet()

// One row per allergen for this product (§9.8). An empty set inserts nothing.
private fun insertAllergensOfProduct(productId: Long, allergens: Set<Allergen>) =
    ProductAllergensTable.batchInsert(allergens) { allergen ->
        this[ProductAllergensTable.productId] = productId
        this[ProductAllergensTable.allergen] = allergen
    }

// Extension function on ResultRow, Exposed's class for one row of a query result.
// Translates that row into the domain model: the bridge between table and object.
// The row must come from a join with SuppliersTable; the allergens live in another table, so the caller passes them.
private fun ResultRow.toProduct(allergens: Set<Allergen>): SurplusProduct = SurplusProduct.create(
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
