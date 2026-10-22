package com.restpartijen.api.reservation

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.update

/**
 * Changes the status directly in ProductsTable, the same way F1's updater does.
 * ProductsTable lives in persistence, so F2 may use it (issue #36); F1's own class may not be imported (GI-5).
 */
internal class TableStatusUpdater : ProductStatusUpdater {
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


    override suspend fun markCollected(productId: Long): Boolean = TODO("not needed here")
    override suspend fun markExpired(productIds: List<Long>): Int = TODO("not needed here")
    override suspend fun markRemoved(productId: Long): Boolean = TODO("not needed here")
}