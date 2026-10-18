package com.restpartijen.api.product.repository

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.update

/**
 * ProductStatusUpdater for other features (K-1, K-6), on the database of F1.
 *
 * Each function is one allowed transition of the state machine (§9.5); a transition that is not
 * listed here cannot be asked for. That is why the contract has separate functions, not one changeStatus.
 */
class ExposedProductStatusUpdater : ProductStatusUpdater {
    override suspend fun markReserved(productId: Long): Boolean =
        changeStatus(productId, fromStatus = ProductStatus.LISTED, toStatus = ProductStatus.RESERVED)

    override suspend fun markListed(productId: Long): Boolean =
        changeStatus(productId, fromStatus = ProductStatus.RESERVED, toStatus = ProductStatus.LISTED)

    override suspend fun markCollected(productId: Long): Boolean =
        changeStatus(productId, fromStatus = ProductStatus.RESERVED, toStatus = ProductStatus.COLLECTED)

    // Returns how many rows really changed: a product reserved in the meantime is no longer LISTED and is skipped.
    override suspend fun markExpired(productIds: List<Long>): Int = dbQuery {
        ProductsTable.update({ (ProductsTable.id inList productIds) and (ProductsTable.status eq ProductStatus.LISTED) }) {
            it[ProductsTable.status] = ProductStatus.EXPIRED
        }
    }

    override suspend fun markRemoved(productId: Long): Boolean =
        changeStatus(productId, fromStatus = ProductStatus.LISTED, toStatus = ProductStatus.REMOVED)
}

// One conditional update: the WHERE checks the current status, so checking and changing happen in one step.
// Two callers at the same time cannot both win: the second finds the status already changed and updates 0 rows.
private suspend fun changeStatus(productId: Long, fromStatus: ProductStatus, toStatus: ProductStatus): Boolean =
    dbQuery {
        ProductsTable.update({ (ProductsTable.id eq productId) and (ProductsTable.status eq fromStatus) }) {
            it[ProductsTable.status] = toStatus
        } > 0
    }