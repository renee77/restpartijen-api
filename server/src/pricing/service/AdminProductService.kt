package com.restpartijen.api.pricing.service

import com.restpartijen.api.persistence.dbQuery
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.NotFoundException
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.shared.ReservationMaintenance

class AdminProductService(
    private val productReader: ProductReader,
    private val productStatusUpdater: ProductStatusUpdater,
    private val reservationMaintenance: ReservationMaintenance,
) {
    suspend fun delete(productId: Long) {
        dbQuery {
            // 404: unknown, or already REMOVED (findById returns null for REMOVED, B-32)
            val product = productReader.findById(productId)
                ?: throw NotFoundException("Product $productId not found")

            when (product.status) {
                ProductStatus.LISTED -> Unit

                // A reserved product first releases its reservation
                ProductStatus.RESERVED -> reservationMaintenance.lapseActiveFor(productId)

                // 409: end states cannot be removed
                ProductStatus.COLLECTED, ProductStatus.EXPIRED, ProductStatus.REMOVED ->
                    throw IllegalStateTransitionException(
                        "Product $productId cannot be removed in status ${product.status}",
                    )
            }

            // false: the product was not LISTED. Throwing here rolls back the lapse above too.
            if (!productStatusUpdater.markRemoved(productId)) {
                throw IllegalStateTransitionException("Product $productId could not be removed")
            }
        }
    }

    suspend fun overview(statuses: Set<ProductStatus>): List<ProductView> {
        if (statuses.isEmpty()) {
            return productReader.findByStatus(ProductStatus.entries.toSet() - ProductStatus.REMOVED)
        } else {
            return productReader.findByStatus(statuses)
        }
    }
}
