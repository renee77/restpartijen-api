package com.restpartijen.api.persistence

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import com.restpartijen.api.product.repository.ProductsTable

object DatabaseFactory {
    fun init() {
        Database.connect(
            url = "jdbc:h2:mem:restpartijen;DB_CLOSE_DELAY=-1",
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