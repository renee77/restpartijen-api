package com.restpartijen.api.reservation

import com.restpartijen.api.product.model.Product
import com.restpartijen.api.reservation.model.ReservationEvent
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.service.ReservationStateMachine
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.ProductStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReservationStateMachineTest {

    // TODO (yours): RESERVE on a LISTED product is allowed and gives RESERVED + ACTIVE
    @Test
    fun `RESERVE on a LISTED product is allowed and gives RESERVED + ACTIVE`() {
        // Arrange
        val event = ReservationEvent.RESERVE
        val product = ProductStatus.LISTED
        val reservation: ReservationStatus? = null
        // Act
        val response = ReservationStateMachine.transition(event, product, reservation)
        // Assert
        assertEquals(ProductStatus.RESERVED, response.product)
        assertEquals(ReservationStatus.ACTIVE, response.reservation)
    }

    @Test
    fun `every combination outside the table of 9_5 is refused`() {
        // Arrange: the four allowed combinations, copied from the table in §9.5
        val allowed = setOf(
            Triple(ReservationEvent.RESERVE, ProductStatus.LISTED, null),
            Triple(ReservationEvent.COLLECT, ProductStatus.RESERVED, ReservationStatus.ACTIVE),
            Triple(ReservationEvent.CANCEL, ProductStatus.RESERVED, ReservationStatus.ACTIVE),
            Triple(ReservationEvent.LAPSE, ProductStatus.RESERVED, ReservationStatus.ACTIVE),
        )
        val reservationStatuses: List<ReservationStatus?> = ReservationStatus.entries + null

        var refused = 0
        for (event in ReservationEvent.entries) {
            for (product in ProductStatus.entries) {
                for (reservation in reservationStatuses) {
                    if (Triple(event, product, reservation) in allowed) continue
                    // Act + Assert: every other combination throws
                    assertFailsWith<IllegalStateTransitionException>("$event / $product / $reservation") {
                        ReservationStateMachine.transition(event, product, reservation)
                    }
                    refused++
                }
            }
        }
        // 4 events x 5 product statuses x 5 reservation statuses (4 + null) = 100, minus 4 allowed
        assertEquals(96, refused)
    }
}