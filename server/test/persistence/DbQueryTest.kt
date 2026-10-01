package com.restpartijen.api.persistence

import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DbQueryTest {

    // A fresh file database per test, so no other test can leave rows behind.
    private val tempDir = createTempDirectory("restpartijen-test")

    @AfterTest
    fun removeTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `a transaction that fails halfway leaves nothing behind`() = runTest {
        // Arrange
        val settings = DatabaseSettings(
            mode = DbMode.FILE,
            filePath = tempDir.resolve("testdb").toString()
        )
        DatabaseFactory.init(settings, TestTable)

        // Act
        // Two writes in one dbQuery; the failure comes after the first one.
        assertFailsWith<IllegalStateException> {
            dbQuery {
                TestTable.insert { it[text] = "first" }
                error("Simulated failure after the first write")
                TestTable.insert { it[text] = "second" }
            }
        }

        // Assert
        val texts = transaction {
            TestTable.selectAll().map { it[TestTable.text] }
        }
        assertEquals(emptyList(), texts)
    }
}
