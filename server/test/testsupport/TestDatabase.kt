package com.restpartijen.api.testsupport

import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode
import com.restpartijen.api.persistence.DatabaseFactory
import org.jetbrains.exposed.v1.core.Table

// Counts the test databases created so far; each call raises it, so every name is new.
private var databaseCount = 0

/**
 * Gives the calling test its own empty in-memory database with [tables] (decision 6.6).
 *
 * Every call uses a new database name, and H2 treats every name as a separate
 * database, so no test can see rows from another test. Production config is not touched.
 */
fun createEmptyTestDatabase(vararg tables: Table) {
    databaseCount++
    DatabaseFactory.init(
        // filePath is only used in file mode, so it stays empty here.
        DatabaseSettings(mode = DbMode.MEMORY, filePath = ""),
        *tables,
        name = "test-${databaseCount}"
    )
}
