package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.shared.ProductStatus
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

/**
 * Products in the database. Deleting is a soft delete: the row stays and gets status REMOVED (§9.5, B-19).
 * A REMOVED product counts as not found, as the Repository contract promises.
 */
class ExposedProductRepository : ProductRepository {
    override suspend fun findById(id: Long): Product? = dbQuery {
        ProductsTable
            .selectAll()
            .where { (ProductsTable.id eq id) and notRemoved() }
            .singleOrNull()
            ?.toProduct()
    }

    override suspend fun findAll(): List<Product> = dbQuery {
        ProductsTable
            .selectAll()
            .where { notRemoved() }
            .map { it.toProduct() }
    }

    override suspend fun create(item: Product): Product = dbQuery {
        // No id and no status here: the database chooses the id, and status defaults to LISTED.
        val newId = ProductsTable.insert {
            it[ProductsTable.name] = item.name
            it[ProductsTable.category] = item.category
        }[ProductsTable.id]
        item.copy(id = newId)
    }

    override suspend fun update(item: Product): Boolean = dbQuery {
        // update() returns how many rows changed: 0 means no product with this id, or it was REMOVED.
        ProductsTable.update({ (ProductsTable.id eq item.id) and notRemoved() }) {
            it[ProductsTable.name] = item.name
            it[ProductsTable.category] = item.category
        } > 0
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

// Translates one database row into the domain model: the bridge between table and object.
private fun ResultRow.toProduct(): Product = Product(
    id = this[ProductsTable.id],
    name = this[ProductsTable.name],
    category = this[ProductsTable.category]
)
