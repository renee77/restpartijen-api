package com.restpartijen.api.persistence

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import com.restpartijen.api.product.repository.ProductsTable
import com.restpartijen.api.config.DatabaseSettings
import com.restpartijen.api.config.DbMode

object DatabaseFactory {
    fun init(settings: DatabaseSettings) {
        val url = when (settings.mode) {
            DbMode.MEMORY -> "jdbc:h2:mem:restpartijen;DB_CLOSE_DELAY=-1"
            DbMode.FILE -> "jdbc:h2:file:${settings.filePath}"
        }
        Database.connect(
            url = url,
            driver = "org.h2.Driver"
        )
        transaction {
            SchemaUtils.create(ProductsTable)
            if (ProductsTable.selectAll().count() == 0L) {
                ProductsTable.insert {
                    it[name] = "Volkoren brood"
                    it[category] = "FRESH"
                }
                ProductsTable.insert {
                    it[name] = "Diepvries spinazie"
                    it[category] = "FROZEN"
                }
            }
        }
    }
}