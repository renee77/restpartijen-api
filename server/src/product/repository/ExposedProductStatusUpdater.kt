package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update

class ExposedProductStatusUpdater : ProductStatusUpdater {
    override suspend fun markReserved(productId: Long): Boolean = dbQuery {
        ProductsTable.update({ (ProductsTable.id eq productId) and (ProductsTable.status eq ProductStatus.LISTED) }) {
            it[ProductsTable.status] = ProductStatus.RESERVED
        } > 0
    }

    override suspend fun markListed(productId: Long): Boolean = dbQuery {
        ProductsTable.update({ (ProductsTable.id eq productId) and (ProductsTable.status eq ProductStatus.RESERVED) }) {
            it[ProductsTable.status] = ProductStatus.LISTED
        } > 0
    }

    override suspend fun markCollected(productId: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun markExpired(productIds: List<Long>): Int {
        TODO("Not yet implemented")
    }

    override suspend fun markRemoved(productId: Long): Boolean {
        TODO("Not yet implemented")
    }
}