package nl.restpartijen.app.core.reservation

import nl.restpartijen.app.core.model.Reservation
import nl.restpartijen.app.core.network.ApiResult

// In src/ until Stefan's real version exists, so others can build on it; then it moves to test/ (FTD §8.3).

/** Answers every reservation with the [result] it was created with, for example a Failure with status 409. */
class FakeReservationRepository(private val result: ApiResult<Reservation>) : ReservationRepository {
    override suspend fun reserve(productId: Long): ApiResult<Reservation> = result
}
