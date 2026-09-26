package com.restpartijen.api.product.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import com.restpartijen.api.product.model.Product

interface ProductRepository {
    fun findById(id: Long): Product?
}

class ExposedProductRepository: ProductRepository {
    override fun findById(id: Long): Product? = transaction {
        ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .singleOrNull()
            ?.toProduct()
    }
}

private fun ResultRow.toProduct(): Product = Product(
    id = this[ProductsTable.id],
    name = this[ProductsTable.name],
    category = this[ProductsTable.category]
)