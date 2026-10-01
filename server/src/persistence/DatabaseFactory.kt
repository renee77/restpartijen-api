package com.restpartijen.api.persistence

import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    /**
     * Connects to the database and creates the given tables if they do not exist yet.
     * The tables come from the caller, so persistence never imports a feature (GI-1).
     */
    fun init(settings: DatabaseSettings, vararg tables: Table) {
        val url = when (settings.mode) {
            DbMode.FILE -> "jdbc:h2:file:${settings.filePath}"
            DbMode.MEMORY -> "jdbc:h2:mem:restpartijen;DB_CLOSE_DELAY=-1"
        }
        Database.connect(
            url = url,
            driver = "org.h2.Driver"
        )
        transaction {
            // `tables` is an array here; the spread operator `*` passes its elements
            // as separate arguments, because create() expects a vararg, not an array.
            SchemaUtils.create(*tables)
        }
    }
}
