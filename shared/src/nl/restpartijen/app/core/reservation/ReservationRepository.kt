package nl.restpartijen.app.core.reservation

import nl.restpartijen.app.core.model.Reservation
import nl.restpartijen.app.core.network.ApiResult

/** Agreement K-A2: Lonneke's reserve button calls this, Stefan builds the real version. */
interface ReservationRepository {
    /** Reserves the product for the logged-in collector (US-05). */
    suspend fun reserve(productId: Long): ApiResult<Reservation>
}
