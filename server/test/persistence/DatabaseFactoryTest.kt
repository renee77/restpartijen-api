package com.restpartijen.api.persistence

import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DatabaseFactoryTest {

    // A fresh temporary folder per test, so the test never touches the real data/ folder.
    private val tempDir = createTempDirectory("restpartijen-test")

    @AfterTest
    fun removeTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `in file mode a row survives a restart`() {
        // Arrange
        val settings = DatabaseSettings(
            mode = DbMode.FILE,
            filePath = tempDir.resolve("testdb").toString()
        )
        DatabaseFactory.init(settings, TestTable)
        transaction {
            TestTable.insert { it[text] = "survives" }
        }

        // Act
        // Connecting again is the restart: the first connection is closed after its
        // transaction, and in file mode H2 then closes the database.
        DatabaseFactory.init(settings, TestTable)
        val texts = transaction {
            TestTable.selectAll().map { it[TestTable.text] }
        }

        // Assert
        assertEquals(listOf("survives"), texts)
        // Without this check, a memory database would also pass: within one JVM,
        // DB_CLOSE_DELAY=-1 keeps it alive across the reconnect.
        assertTrue(tempDir.resolve("testdb.mv.db").exists())
    }
}
