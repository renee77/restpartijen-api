package com.restpartijen.api.testsupport

import com.restpartijen.api.persistence.Repository

/**
 * In-memory Repository for tests: shows the interface works without a database (E3).
 * Deleting removes the item; soft-delete is left to the real implementations.
 */
class FakeRepository<T>(
    private val idOf: (T) -> Long,
    private val withId: (T, Long) -> T,
    initialItems: List<T> = emptyList()
) : Repository<T> {

    private val items = initialItems.toMutableList()

    // Continue after the highest id already present, like an auto-increment column.
    private var nextId = (initialItems.maxOfOrNull(idOf) ?: 0L) + 1

    override suspend fun findById(id: Long): T? = items.find { idOf(it) == id }

    // Return a copy, so a test cannot change the fake's contents behind its back.
    override suspend fun findAll(): List<T> = items.toList()

    override suspend fun create(item: T): T {
        val stored = withId(item, nextId++)
        items.add(stored)
        return stored
    }

    override suspend fun update(item: T): Boolean {
        val index = items.indexOfFirst { idOf(it) == idOf(item) }
        if (index == -1) return false
        items[index] = item
        return true
    }

    override suspend fun delete(id: Long): Boolean = items.removeIf { idOf(it) == id }
}
