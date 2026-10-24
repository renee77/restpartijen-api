package com.restpartijen.api.product.service

import com.restpartijen.api.product.model.OpeningHours
import com.restpartijen.api.product.repository.SupplierRepository
import com.restpartijen.api.shared.DomainRuleException
import com.restpartijen.api.shared.ForbiddenException
import com.restpartijen.api.shared.NotFoundException

/**
 * The domain logic of US-10: reading and replacing the opening hours of a supplier (§5.10, §9.9).
 *
 * Calculating the pickup window is not done here: PickupWindowCalculator does that,
 * and ProductService reads the week straight from [suppliers].
 *
 * @param suppliers stores the opening hours and knows whether a supplier exists
 * @param ownershipGuard checks that a supplier only replaces its own opening hours (§16.2)
 */
class OpeningHoursService(private val suppliers: SupplierRepository, private val ownershipGuard: OwnershipGuard) {
    /**
     * The opening hours of [supplierId], from Monday to Sunday; a closed day is not in the list.
     *
     * @throws NotFoundException when the supplier is unknown (404)
     */
    suspend fun getOpeningHours(supplierId: Long): List<OpeningHours> {
        // Checked separately: without it, an unknown supplier would look like a supplier that is always closed.
        if (!suppliers.exists(supplierId)) throw NotFoundException("Supplier not found")

        return suppliers.getOpeningHours(supplierId)
    }

    /**
     * Replaces the whole week of [supplierId] for the user [userId] and returns the stored week.
     *
     * @throws NotFoundException when the supplier is unknown (404)
     * @throws ForbiddenException when the supplier is not the supplier of [userId] (403)
     * @throws DomainRuleException when [week] is empty, has a day twice, or closes before it opens (422)
     */
    suspend fun replaceOpeningHours(
        userId: Long,
        supplierId: Long,
        week: List<OpeningHoursCommand>,
    ): List<OpeningHours> {
        // 404 before 403, as with products (US-03); ownership before the week, so only the owner hears about it.
        if (!suppliers.exists(supplierId)) throw NotFoundException("Supplier not found")
        ownershipGuard.requireOwnSupplier(userId, supplierId)

        // A supplier always has opening hours (decision 23 Oct 2026); an empty week would mean "never open".
        if (week.isEmpty()) {
            throw DomainRuleException("openingHours must contain at least one open day", field = "openingHours")
        }
        // Checked here, not left to the primary key: a database error would be a 500 without the day.
        val duplicateDays = week.groupBy { it.day }.filterValues { it.size > 1 }.keys
        if (duplicateDays.isNotEmpty()) {
            throw DomainRuleException("openingHours has a day more than once: $duplicateDays", field = "openingHours")
        }
        // Built only now, after the checks above: OpeningHours refuses closing before opening itself (422).
        val openingHours = week.map { OpeningHours(it.day, it.opensAt, it.closesAt) }

        suppliers.updateOpeningHours(supplierId, openingHours)
        // The stored week, from Monday to Sunday, so the answer shows what is really saved (§5.10).
        return suppliers.getOpeningHours(supplierId)
    }
}
