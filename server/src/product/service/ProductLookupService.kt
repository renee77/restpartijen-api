package com.restpartijen.api.product.service

import com.restpartijen.api.product.client.LookupResult
import com.restpartijen.api.product.client.OpenFoodFactsClient
import com.restpartijen.api.product.model.MAX_TEXT_LENGTH
import com.restpartijen.api.shared.NotFoundException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

// Allergens on Open Food Facts rarely change; after a day a correction there still comes through
// (decision Eva, 22 Oct).
private val FOUND_TIME_TO_LIVE = 24.hours

// Short, because someone may add the product to Open Food Facts; long enough to absorb retries (decision Eva, 22 Oct).
private val NOT_FOUND_TIME_TO_LIVE = 60.seconds

// Enough for every barcode in a demo or a busy day, small enough to never fill the memory.
private const val MAX_BARCODES = 1_000

/**
 * The barcode lookup of US-02 (§5.2, SD-1): a suggestion from Open Food Facts before placing a product.
 * Its own service, apart from ProductService, because it shares nothing with managing products (code review, round 2).
 *
 * The order keeps calls to Open Food Facts rare: a found product (24 hours), then a known unknown barcode
 * (60 seconds), then the limit of 10 per minute and 5 per supplier, and only then the call itself.
 *
 * @param openFoodFacts fetches the name and allergen tags for a barcode (§10.3)
 * @param clock decides when a cached answer expires and when a call leaves the limit window (GI-6)
 */
class ProductLookupService(private val openFoodFacts: OpenFoodFactsClient, clock: Clock) {
    private val found = ExpiringCache<ProductLookup>(clock, FOUND_TIME_TO_LIVE, MAX_BARCODES)

    // Nothing to store but the fact that the barcode is unknown.
    private val notFound = ExpiringCache<Unit>(clock, NOT_FOUND_TIME_TO_LIVE, MAX_BARCODES)

    private val rateLimiter = OpenFoodFactsRateLimiter(clock)

    /**
     * The suggestion for a product with this [barcode], looked up for the supplier with [userId].
     *
     * @return the name and allergens; manualEntryRequired is true when Open Food Facts gave no usable answer
     *   or a limit of calls per minute is reached
     * @throws NotFoundException when Open Food Facts does not know the barcode (404)
     */
    suspend fun lookup(barcode: String, userId: Long): ProductLookup {
        found.get(barcode)?.let { return it }
        if (notFound.get(barcode) != null) throw NotFoundException(NOT_FOUND_MESSAGE)
        // Over a limit counts as no answer: the supplier fills in the product, as during a time-out.
        if (!rateLimiter.tryAcquire(userId)) return ProductLookup.MANUAL_ENTRY

        return when (val result = openFoodFacts.fetchProduct(barcode)) {
            is LookupResult.Found -> {
                val lookup = ProductLookup(
                    // A blank or too long name becomes null: cutting it off would look usable but be half a name.
                    name = result.name?.takeIf { it.isNotBlank() && it.length <= MAX_TEXT_LENGTH },
                    allergens = mapAllergenTags(result.allergenTags),
                    manualEntryRequired = false,
                )
                found.put(barcode, lookup)
                lookup
            }

            // Only briefly remembered: someone may add the product to Open Food Facts later.
            LookupResult.NotFound -> {
                notFound.put(barcode, Unit)
                throw NotFoundException(NOT_FOUND_MESSAGE)
            }

            // Not cached, or a time-out or rate limit would stick.
            LookupResult.Unavailable -> ProductLookup.MANUAL_ENTRY
        }
    }

    private companion object {
        const val NOT_FOUND_MESSAGE = "No product found for this barcode"
    }
}
