package com.restpartijen.api.testsupport

import com.restpartijen.api.persistence.ProductsTable
import com.restpartijen.api.persistence.SuppliersTable
import com.restpartijen.api.persistence.UsersTable
import com.restpartijen.api.shared.ProductCategory
import com.restpartijen.api.shared.Role
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

// Rows for the shared tables in persistence (issue #36). With foreign keys, a test that stores a row
// must first store the rows it refers to; these helpers give each a valid row and return its id.
// Only for tables in persistence: every feature may import that package, so every test may use these.

/** Counts the users created so far, so every e-mail address is new (the column is unique). */
private var userCount = 0

/** Stores a valid user and returns its id. */
fun insertUser(role: Role = Role.COLLECTOR): Long = transaction {
    userCount++
    UsersTable.insert {
        it[email] = "user-$userCount@test.nl"
        it[passwordHash] = "not-a-real-hash"
        it[displayName] = "Test user $userCount"
        it[UsersTable.role] = role
        it[createdAt] = Instant.parse("2026-10-01T00:00:00Z")
    }[UsersTable.id]
}

/** Stores a valid supplier for [userId] and returns its id. */
fun insertSupplier(userId: Long): Long = transaction {
    SuppliersTable.insert {
        it[SuppliersTable.userId] = userId
        it[name] = "Bakkerij Test"
        it[address] = "Teststraat 1, Breda"
        it[latitude] = 51.59
        it[longitude] = 4.78
    }[SuppliersTable.id]
}

/**
 * Stores a valid product and returns its id.
 * A product needs a supplier (foreign key); without [supplierId] a new supplier with its own user is stored first.
 */
fun insertProduct(supplierId: Long = insertSupplier(insertUser(Role.SUPPLIER))): Long = transaction {
    val now = Instant.parse("2026-10-01T00:00:00Z")
    ProductsTable.insert {
        it[ProductsTable.supplierId] = supplierId
        it[category] = ProductCategory.FRESH
        it[name] = "Volkoren brood"
        it[originalPriceCents] = 350
        it[bestBeforeAt] = now + 3.days
        it[quantity] = 1
        it[pickupFrom] = now
        it[pickupUntil] = now + 2.days
        it[createdAt] = now
    }[ProductsTable.id]
}
