package com.restpartijen.api.persistence

/**
 * Generic persistence contract for entities with a Long id.
 *
 * Internal to the persistence layer: other features never see other repositories.
 * They get read-only access through the interfaces in shared (K-1, K-6).
 *
 * Deleted items count as not found: no function returns them.
 */
interface Repository<T> {

    /** The item with this id, or null if it does not exist or was deleted. */
    suspend fun findById(id: Long): T?

    /** All items that are not deleted. */
    suspend fun findAll(): List<T>

    /**
     * Stores a new item.
     * Returns the stored item, including the id the database generated for a new item.
     */
    suspend fun create(item: T): T

    /**
     * Replaces the stored item that has the same id as [item].
     * Returns false if there was nothing to update: unknown id or already deleted.
     */
    suspend fun update(item: T): Boolean

    /**
     * Deletes the item with this id.
     * Implementations may soft-delete (for products: status REMOVED, §9.5).
     * Returns false if there was nothing to delete: unknown id or already deleted.
     */
    suspend fun delete(id: Long): Boolean
}