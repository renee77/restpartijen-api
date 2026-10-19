package com.restpartijen.api.seed

import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.security.service.PasswordHasher
import com.restpartijen.api.shared.Role
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock

private data class SeedUser(
    val email: String,
    val displayName: String,
    val role: Role,
)

// ownerEmail instead of a user id: the database chooses the id, so the data can only name the account.
private data class SeedSupplier(
    val ownerEmail: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

object SeedData {
    private val users = listOf(
        SeedUser("admin@mail.com", "Pad Min", Role.ADMIN),
        SeedUser("bakker@mail.com", "Bakkerij De Korenschoof", Role.SUPPLIER),
    )

    private val suppliers = listOf(
        SeedSupplier("bakker@mail.com", "Bakkerij De Korenschoof", "Ginnekenweg 12, Breda", 51.5795, 4.7820),
    )

    // Demo only: public on purpose, because README.md and requests.http name it for every seed account.
    private const val DEMO_PASSWORD = "Demo-Restpartij-2026"

    fun seedIfEmpty(clock: Clock, hasher: PasswordHasher) {
        val isEmpty = transaction { UsersTable.selectAll().empty() }
        if (!isEmpty) return

        transaction {
            // Email → id, filled while inserting: the database chooses the ids, the data only knows the emails.
            val userIds = mutableMapOf<String, Long>()
            val supplierIds = mutableMapOf<String, Long>()

            for (user in users) {
                userIds[user.email] =
                    UsersTable.insert {
                        it[UsersTable.email] = user.email
                        it[UsersTable.displayName] = user.displayName
                        it[UsersTable.role] = user.role
                        it[UsersTable.passwordHash] = hasher.hashPassword(DEMO_PASSWORD)
                        it[UsersTable.createdAt] = clock.now()
                    }[UsersTable.id]
            }

            for (supplier in suppliers) {
                supplierIds[supplier.ownerEmail] =
                    SuppliersTable.insert {
                        // getValue fails loudly on a typo in ownerEmail, instead of storing a wrong link.
                        it[SuppliersTable.userId] = userIds.getValue(supplier.ownerEmail)
                        it[SuppliersTable.name] = supplier.name
                        it[SuppliersTable.address] = supplier.address
                        it[SuppliersTable.latitude] = supplier.latitude
                        it[SuppliersTable.longitude] = supplier.longitude
                    }[SuppliersTable.id]
            }
        }
    }
}