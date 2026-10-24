package com.restpartijen.api.reservation

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.reservation.model.Reservation
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ReservationRepository
import com.restpartijen.api.shared.Money
import com.restpartijen.api.shared.PickupWindow
import com.restpartijen.api.shared.PriceBreakdown
import com.restpartijen.api.shared.PriceProvider
import com.restpartijen.api.shared.PricedProduct
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.ProductReader
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatusUpdater
import com.restpartijen.api.shared.ProductView
import com.restpartijen.api.shared.SupplierSummary
import com.restpartijen.api.testsupport.FakeRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A product as F2 sees it, with sensible defaults so a test only sets what matters. */
data class FakeProduct(
    override val id: Long,
    override val pickupWindow: PickupWindow,
    override val status: ProductStatus = ProductStatus.LISTED,
    override val name: String = "Volkoren brood",
    override val supplier: SupplierSummary = SupplierSummary(1, "Bakkerij De Korenaar", 51.69, 5.30),
    override val originalPrice: Money = Money.fromCents(349),
    override val category: ProductCategory = ProductCategory.FRESH,
) : ProductView {
    override fun shelfLifeRemaining(clock: Clock): Duration = pickupWindow.until - clock.now()
}

class FakeProductReader(private val products: List<ProductView>) : ProductReader {
    override suspend fun findById(id: Long): ProductView? = products.find {
        it.id == id &&
            it.status != ProductStatus.REMOVED
    }
    override suspend fun findAvailable(now: Instant): List<ProductView> =
        products.filter { it.status == ProductStatus.LISTED }

    override suspend fun findExpiredListings(now: Instant): List<ProductView> = emptyList()
    override suspend fun findByStatus(statuses: Set<ProductStatus>): List<ProductView> =
        products.filter { it.status in statuses }
}

/** Records which products were reserved, so a test can check the service really called it. */
class FakeProductStatusUpdater : ProductStatusUpdater {
    val reserved = mutableListOf<Long>()
    val listed = mutableListOf<Long>()
    val collected = mutableListOf<Long>()
    override suspend fun markReserved(productId: Long): Boolean = reserved.add(productId)
    override suspend fun markListed(productId: Long): Boolean = listed.add(productId)
    override suspend fun markCollected(productId: Long): Boolean = collected.add(productId)
    override suspend fun markExpired(productIds: List<Long>): Int = 0
    override suspend fun markRemoved(productId: Long): Boolean = false
}

/** Always returns the same price; null means "expired", as in the real contract. */
class FakePriceProvider(private val price: Money?) : PriceProvider {
    override fun currentPrice(product: PricedProduct): Money? = price
    override fun priceBreakdown(product: PricedProduct): PriceBreakdown? = price?.let { PriceBreakdown(it, 0, it) }
}

/** In-memory reservations, reusing Eva's generic FakeRepository via delegation. */
class FakeReservationRepository(initial: List<Reservation> = emptyList()) :
    ReservationRepository,
    Repository<Reservation> by FakeRepository(
        idOf = { it.id },
        withId = { r, id -> r.copy(id = id) },
        initialItems = initial,
    ) {

    override suspend fun findOverdue(now: Instant): List<Reservation> =
        findAll().filter { it.status == ReservationStatus.ACTIVE && it.expiresAt <= now }

    override suspend fun findActiveByProductId(productId: Long): Reservation? =
        findAll().firstOrNull { it.status == ReservationStatus.ACTIVE && it.productId == productId }

    override suspend fun findByCollectorId(collectorId: Long): List<Reservation> =
        findAll().filter { it.collectorId == collectorId }.sortedByDescending { it.reservedAt }
}
