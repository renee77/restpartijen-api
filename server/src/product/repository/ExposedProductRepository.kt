package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.Allergen
import com.restpartijen.api.shared.ProductStatus
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock

/**
 * Products in the database. Deleting is a soft delete: the row stays and gets status REMOVED (§9.5, B-19).
 * Only a LISTED product can be deleted; delete returns false for any other status (§5.3).
 * A REMOVED product counts as not found, as the Repository contract promises.
 *
 * @property clock sets created_at when a product is stored
 */
class ExposedProductRepository(private val clock: Clock) : ProductRepository {
    override suspend fun findById(id: Long): SurplusProduct? = dbQuery {
        // The allergens live in their own table, so they are read with the id of the row that was found.
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

    /** Like delete, it changes only a LISTED product: false also means the product is not LISTED (A13). */
    override suspend fun update(item: SurplusProduct): Boolean = dbQuery {
        // update() returns how many rows changed: 0 means no product with this id, or it is not LISTED (A13).
        // The status check sits in the WHERE, so a product reserved after the check in the service stays
        // unchanged (race). Supplier, created_at and status stay as they are; status changes go through
        // ProductStatusUpdater.
        val updated =
            ProductsTable.update({ (ProductsTable.id eq item.id) and (ProductsTable.status eq ProductStatus.LISTED) }) {
                writeChangeableColumns(it, item)
            } > 0
        // Only for an existing, LISTED product. Replace the whole set:
        // simpler than working out which allergens were added or removed.
        if (updated) {
            ProductAllergensTable.deleteWhere { productId eq item.id }
            insertAllergensOfProduct(item.id, item.allergens)
        }
        updated
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        // Soft delete: only the status changes, and only LISTED may become REMOVED (§5.3, §9.5).
        // Any other status, REMOVED included, matches no row and gives false.
        ProductsTable.update({ (ProductsTable.id eq id) and (ProductsTable.status eq ProductStatus.LISTED) }) {
            it[ProductsTable.status] = ProductStatus.REMOVED
        } > 0
    }

    override suspend fun findBySupplier(supplierId: Long): List<SurplusProduct> = dbQuery {
        (ProductsTable innerJoin SuppliersTable).selectAll()
            .where {
                (ProductsTable.supplierId eq supplierId) and
                    ((ProductsTable.status eq ProductStatus.LISTED) or (ProductsTable.status eq ProductStatus.RESERVED))
            }
            .orderBy(ProductsTable.id)
            .map { it.toProduct(allergensOfProduct(it[ProductsTable.id])) }
    }
}

/**
 * Writes the columns that both create and update write. Supplier, created_at and status are left out:
 * update must not change them, and status changes go through ProductStatusUpdater.
 * UpdateBuilder is the common parent of Exposed's insert and update statements, so one function serves both.
 */
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

/** Inserts one row per allergen for this product (§9.8). An empty set inserts nothing. */
private fun insertAllergensOfProduct(productId: Long, allergens: Set<Allergen>) =
    ProductAllergensTable.batchInsert(allergens) { allergen ->
        this[ProductAllergensTable.productId] = productId
        this[ProductAllergensTable.allergen] = allergen
    }
