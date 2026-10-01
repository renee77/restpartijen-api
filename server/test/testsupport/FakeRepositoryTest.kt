package com.restpartijen.api.testsupport

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

// Minimal entity for these tests, so the generic fake is tested without depending on a feature.
private data class Item(val id: Long, val name: String)

class FakeRepositoryTest {

    // Builds a fake for Item, so each test only states its starting data.
    private fun repositoryWith(vararg items: Item) = FakeRepository<Item>(
        idOf = { it.id },
        withId = { item, id -> item.copy(id = id) },
        initialItems = items.toList()
    )

    @Test
    fun `findById returns the item when the id exists`() = runTest {
        // Arrange
        val repository = repositoryWith(Item(id = 1, name = "Volkoren brood"))

        // Act
        val item = repository.findById(1)

        // Assert
        assertEquals(Item(id = 1, name = "Volkoren brood"), item)
    }

    @Test
    fun `findAll returns all items`() = runTest {
        // Arrange
        val repository = repositoryWith(
            Item(id = 1, name = "Volkoren brood"),
            Item(id = 2, name = "Aardappels")
        )

        // Act
        val items = repository.findAll()

        // Assert
        assertEquals(
            listOf(
                Item(id = 1, name = "Volkoren brood"),
                Item(id = 2, name = "Aardappels")
            ),
            items
        )
    }

    @Test
    fun `create gives the first item id 1 and stores it`() = runTest {
        // Arrange
        val repository = repositoryWith()
        // Id 0 is never chosen by the fake, so id 1 in the result proves the fake assigned it.
        val newItem = Item(id = 0, name = "Volkoren brood")

        // Act
        val created = repository.create(newItem)

        // Assert
        assertEquals(Item(id = 1, name = "Volkoren brood"), created)
        assertEquals(created, repository.findById(1))
    }

    @Test
    fun `update replaces only the item with the same id and returns true`() = runTest {
        // Arrange
        val repository = repositoryWith(
            Item(id = 1, name = "Volkoren brood"),
            Item(id = 2, name = "Aardappels")
        )

        // Act
        val updateSucceeded = repository.update(Item(id = 2, name = "Wit brood"))

        // Assert
        assertTrue(updateSucceeded)
        assertEquals(
            listOf(
                Item(id = 1, name = "Volkoren brood"),
                Item(id = 2, name = "Wit brood")
            ),
            repository.findAll()
        )
    }

    @Test
    fun `findById returns null when the id does not exist`() = runTest {
        // Arrange
        val repository = repositoryWith(Item(id = 1, name = "Volkoren brood"))

        // Act
        val item = repository.findById(999)

        // Assert
        assertNull(item)
    }

    @Test
    fun `findAll returns an empty list when the repository is empty`() = runTest {
        // Arrange
        val repository = repositoryWith()

        // Act
        val items = repository.findAll()

        // Assert
        assertEquals(emptyList(), items)
    }

    @Test
    fun `create gives the next id after the highest existing id`() = runTest {
        // Arrange
        // Ids 1 and 5: the next id must be 6, not 3 (the number of items plus one).
        val repository = repositoryWith(
            Item(id = 1, name = "Volkoren brood"),
            Item(id = 5, name = "Aardappels")
        )

        // Act
        val created = repository.create(Item(id = 0, name = "Wit brood"))

        // Assert
        assertEquals(Item(id = 6, name = "Wit brood"), created)
    }

    @Test
    fun `create ignores the id of the incoming item`() = runTest {
        // Arrange
        val repository = repositoryWith()

        // Act
        val created = repository.create(Item(id = 99, name = "Volkoren brood"))

        // Assert
        assertEquals(Item(id = 1, name = "Volkoren brood"), created)
        assertNull(repository.findById(99))
    }

    @Test
    fun `create does not reuse the id of a deleted item`() = runTest {
        // Arrange
        val repository = repositoryWith(
            Item(id = 1, name = "Volkoren brood"),
            Item(id = 2, name = "Aardappels")
        )
        repository.delete(2)

        // Act
        val created = repository.create(Item(id = 0, name = "Wit brood"))

        // Assert
        assertEquals(Item(id = 3, name = "Wit brood"), created)
    }

    @Test
    fun `update returns false and changes nothing when the id does not exist`() = runTest {
        // Arrange
        val repository = repositoryWith(Item(id = 1, name = "Volkoren brood"))

        // Act
        val updateSucceeded = repository.update(Item(id = 999, name = "Wit brood"))

        // Assert
        assertFalse(updateSucceeded)
        assertEquals(listOf(Item(id = 1, name = "Volkoren brood")), repository.findAll())
    }

    @Test
    fun `delete removes only the item with the given id and returns true`() = runTest {
        // Arrange
        val repository = repositoryWith(
            Item(id = 1, name = "Volkoren brood"),
            Item(id = 2, name = "Aardappels")
        )

        // Act
        val deleteSucceeded = repository.delete(1)

        // Assert
        assertTrue(deleteSucceeded)
        assertNull(repository.findById(1))
        assertEquals(listOf(Item(id = 2, name = "Aardappels")), repository.findAll())
    }

    @Test
    fun `delete returns false when the id does not exist`() = runTest {
        // Arrange
        val repository = repositoryWith(Item(id = 1, name = "Volkoren brood"))

        // Act
        val deleteSucceeded = repository.delete(999)

        // Assert
        assertFalse(deleteSucceeded)
        assertEquals(listOf(Item(id = 1, name = "Volkoren brood")), repository.findAll())
    }

    @Test
    fun `delete returns false the second time for the same id`() = runTest {
        // Arrange
        val repository = repositoryWith(Item(id = 1, name = "Volkoren brood"))
        repository.delete(1)

        // Act
        val secondDelete = repository.delete(1)

        // Assert
        assertFalse(secondDelete)
    }
}
