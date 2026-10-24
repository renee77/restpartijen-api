package com.restpartijen.api.persistence

import com.restpartijen.api.reservation.model.ReservationStatus
import com.restpartijen.api.reservation.repository.ReservationsTable
import com.restpartijen.api.testsupport.createEmptyTestDatabase
import com.restpartijen.api.testsupport.insertProduct
import com.restpartijen.api.testsupport.insertUser
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant

/**
 * The database itself refuses a reference to a row that does not exist (issue #36).
 * Each test makes every other reference valid, so only the reference under test can cause the failure.
 */
class ForeignKeyTest {

    /** An id that no row has: every test starts with an empty database. */
    private val unknownId = 999L

    @Test
    fun `a supplier must belong to an existing user`() {
        // Arrange
        createEmptyTestDatabase(UsersTable, SuppliersTable)

        // Act + Assert
        assertFailsWith<ExposedSQLException> {
            transaction {
                SuppliersTable.insert {
                    it[userId] = unknownId
                    it[name] = "Bakkerij Test"
                    it[address] = "Teststraat 1, Breda"
                    it[latitude] = 51.59
                    it[longitude] = 4.78
                }
            }
        }
    }

    @Test
    fun `a reservation must point to an existing product`() {
        // Arrange
        createEmptyTestDatabase(UsersTable, ProductsTable, ReservationsTable)
        val collectorId = insertUser()

        // Act + Assert
        assertFailsWith<ExposedSQLException> { insertReservation(productId = unknownId, collectorId = collectorId) }
    }

    @Test
    fun `a reservation must point to an existing collector`() {
        // Arrange
        createEmptyTestDatabase(UsersTable, ProductsTable, ReservationsTable)
        val productId = insertProduct()

        // Act + Assert
        assertFailsWith<ExposedSQLException> { insertReservation(productId = productId, collectorId = unknownId) }
    }

    /** Stores an ACTIVE reservation with fixed values, pointing at [productId] and [collectorId]. */
    private fun insertReservation(productId: Long, collectorId: Long) = transaction {
        ReservationsTable.insert {
            it[ReservationsTable.productId] = productId
            it[ReservationsTable.collectorId] = collectorId
            it[reservedPriceCents] = 199
            it[reservedAt] = Instant.parse("2026-10-01T12:00:00Z")
            it[expiresAt] = Instant.parse("2026-10-02T12:00:00Z")
            it[status] = ReservationStatus.ACTIVE
        }
    }
}
