package com.restpartijen.api.product.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import com.restpartijen.api.product.model.Product
import com.restpartijen.api.persistence.Repository
import com.restpartijen.api.persistence.dbQuery
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.update

interface ProductRepository : Repository<Product>

class ExposedProductRepository : ProductRepository {
    override suspend fun findById(id: Long): Product? = dbQuery {
        ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .singleOrNull()
            ?.toProduct()
    }

    override suspend fun findAll(): List<Product> = dbQuery {
        ProductsTable
            .selectAll()
            .map { it.toProduct() }
    }

    override suspend fun create(item: Product): Product = dbQuery {
        // No id here: the column is autoIncrement, so the database chooses it.
        val newId = ProductsTable.insert {
            it[ProductsTable.name] = item.name
            it[ProductsTable.category] = item.category
        }[ProductsTable.id]
        item.copy(id = newId)
    }

    override suspend fun update(item: Product): Boolean = dbQuery {
        // update() returns how many rows changed: 0 means no product with this id.
        ProductsTable.update({ ProductsTable.id eq item.id }) {
            it[ProductsTable.name] = item.name
            it[ProductsTable.category] = item.category
        } > 0
    }

    override suspend fun delete(id: Long): Boolean = dbQuery {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }
}

// Translates one database row into the domain model: the bridge between table and object.
private fun ResultRow.toProduct(): Product = Product(
    id = this[ProductsTable.id],
    name = this[ProductsTable.name],
    category = this[ProductsTable.category]
)

