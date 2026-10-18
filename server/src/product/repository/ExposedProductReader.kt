package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductView
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.time.Instant

/**
 * ProductReader for other features (K-1, K-6), on the database of F1.
 * findById passes on to the repository, so both promise exactly the same.
 */
class ExposedProductReader(private val repository: ProductRepository) : ProductReader {
    override suspend fun findById(id: Long): ProductView? =
        repository.findById(id)

    override suspend fun findAvailable(now: Instant): List<ProductView> =
        productsWhere((ProductsTable.status eq ProductStatus.LISTED) and (ProductsTable.bestBeforeAt greater now))

    override suspend fun findExpiredListings(now: Instant): List<ProductView> =
        productsWhere((ProductsTable.status eq ProductStatus.LISTED) and (ProductsTable.bestBeforeAt lessEq now))

    override suspend fun findByStatus(statuses: Set<ProductStatus>): List<ProductView> =
        productsWhere(ProductsTable.status inList statuses)
}

// Every query of this reader: products with their supplier, filtered by [condition].
// Each function above only decides the condition; fetching and mapping rows happens here, in one transaction.
private suspend fun productsWhere(condition: Op<Boolean>): List<ProductView> = dbQuery {
    (ProductsTable innerJoin SuppliersTable).selectAll()
        .where { condition }
        .map { row -> row.toProduct(allergensOfProduct(row[ProductsTable.id])) }
}
