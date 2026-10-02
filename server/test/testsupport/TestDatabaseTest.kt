package com.restpartijen.api.testsupport

import com.restpartijen.api.persistence.TestTable
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals

// Two nearly identical tests: if they shared a database, whichever runs second would count 2 rows.
class TestDatabaseTest {
    @Test
    fun `first test sees only its own row`() {
        // Arrange
        createEmptyTestDatabase(TestTable)

        // Act
        transaction {
            TestTable.insert { it[text] = "first" }
        }

        // Assert
        val rowCount = transaction { TestTable.selectAll().count() }
        assertEquals(1L, rowCount)
    }

    @Test
    fun `second test sees only its own row`() {
        // Arrange
        createEmptyTestDatabase(TestTable)

        // Act
        transaction {
            TestTable.insert { it[text] = "second" }
        }

        // Assert
        val rowCount = transaction { TestTable.selectAll().count() }
        assertEquals(1L, rowCount)
    }
}
