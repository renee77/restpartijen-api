package com.restpartijen.api.persistence

import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

/**
 * Opens the H2 database for the application and for the tests (GI-1).
 *
 * The one place that builds the JDBC URL, so production and tests open the database the same way.
 */
object DatabaseFactory {
    /**
     * Connects to the database and creates the given tables if they do not exist yet.
     * The tables come from the caller, so persistence never imports a feature (GI-1).
     *
     * [name] is only used in memory mode. Production keeps the default; tests pass a
     * unique name, so every test gets its own empty database (decision 6.6).
     *
     * @param settings memory or file mode, and the file path for file mode.
     * @param tables the tables to create, in any order: SchemaUtils.create sorts them by their foreign keys.
     */
    fun init(settings: DatabaseSettings, vararg tables: Table, name: String = "restpartijen") {
        val url = when (settings.mode) {
            DbMode.FILE -> "jdbc:h2:file:${settings.filePath}"
            DbMode.MEMORY -> "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1"
        }
        Database.connect(
            url = url,
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(*tables)
        }
    }
}
