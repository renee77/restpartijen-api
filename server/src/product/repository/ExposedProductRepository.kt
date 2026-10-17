package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.product.model.SurplusProduct
import com.restpartijen.api.shared.ProductStatus
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.jdbc.update

/**
 * Products in the database. Deleting is a soft delete: the row stays and gets status REMOVED (§9.5, B-19).
 * A REMOVED product counts as not found, as the Repository contract promises.
 */
class ExposedProductRepository : ProductRepository {
    override suspend fun findById(id: Long): SurplusProduct? = TODO("Blok c")

    override suspend fun findAll(): List<SurplusProduct> = TODO("Blok c")

    override suspend fun create(item: SurplusProduct): SurplusProduct = TODO("Blok c")

    override suspend fun update(item: SurplusProduct): Boolean = TODO("Blok c")

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
private fun ResultRow.toProduct(): SurplusProduct = TODO("Blok c")
