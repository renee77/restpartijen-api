package com.restpartijen.api.reservation.repository

import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.reservation.model.Reservation

/** Persistence of reservations; implements the generic contract from GI-1 (§11.2). */
interface ReservationRepository : Repository<Reservation>