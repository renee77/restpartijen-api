package com.restpartijen.api.reservation.service

import com.restpartijen.api.reservation.model.ReservationEvent
import com.restpartijen.api.reservation.model.ReservationEvent.CANCEL
import com.restpartijen.api.reservation.model.ReservationEvent.COLLECT
import com.restpartijen.api.reservation.model.ReservationEvent.LAPSE
import com.restpartijen.api.reservation.model.ReservationEvent.RESERVE
import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.model.ReservationStatus.ACTIVE
import com.restpartijen.api.reservation.model.ReservationStatus.CANCELLED
import com.restpartijen.api.reservation.model.ReservationStatus.LAPSED
import com.restpartijen.api.shared.IllegalStateTransitionException
import com.restpartijen.api.shared.ProductStatus
import com.restpartijen.api.shared.ProductStatus.LISTED
import com.restpartijen.api.shared.ProductStatus.RESERVED

/**
 * Decides whether an event is allowed for the current product and reservation status (§9.5).
 * The table below is the table from the FTD; every other combination is refused.
 */
object ReservationStateMachine {

    /**
     * Where a transition starts. reservation is null for RESERVE: there is no reservation yet.
     *
     * @property event what happens
     * @property product the current product status
     * @property reservation the current reservation status, or null when there is no reservation yet
     */
    private data class From(
        val event: ReservationEvent,
        val product: ProductStatus,
        val reservation: ReservationStatus?,
    )

    /**
     * Where a transition ends: both statuses change together, in one transaction.
     *
     * @property product the new product status
     * @property reservation the new reservation status
     */
    data class To(val product: ProductStatus, val reservation: ReservationStatus)

    /** The allowed transitions; a combination that is not a key here is refused. */
    private val transitions: Map<From, To> = mapOf(
        From(RESERVE, LISTED, null) to To(RESERVED, ACTIVE),
        From(COLLECT, RESERVED, ACTIVE) to To(ProductStatus.COLLECTED, ReservationStatus.COLLECTED),
        From(CANCEL, RESERVED, ACTIVE) to To(LISTED, CANCELLED),
        From(LAPSE, RESERVED, ACTIVE) to To(LISTED, LAPSED),
    )

    /**
     * The new statuses after [event], given the current [product] and [reservation] status.
     * Changes nothing itself: the caller stores both new statuses.
     *
     * @throws IllegalStateTransitionException when the combination is not allowed (409)
     */
    fun transition(event: ReservationEvent, product: ProductStatus, reservation: ReservationStatus?): To =
        transitions[From(event, product, reservation)]
            ?: throw IllegalStateTransitionException(
                "$event is not allowed for product $product and reservation $reservation",
            )
}
