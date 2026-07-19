package com.chibaminto.wearcost.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomCategoryRepositoryTest {
    private lateinit var database: WearCostDatabase
    private lateinit var repository: ClothingRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            WearCostDatabase::class.java
        ).build()
        repository = ClothingRepository(database.clothingDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addCustomCategory_returnsCreatedCategoryAndPersistsIt() = runBlocking {
        val created = repository.addCustomCategory("Work")

        assertNotNull(created)
        assertEquals("Work", created?.name)
        assertEquals(listOf("Work"), repository.customCategories.first().map { it.name })
    }

    @Test
    fun addCustomCategory_trimsNameAndRejectsBlank() = runBlocking {
        val blank = repository.addCustomCategory("   ")
        val created = repository.addCustomCategory("  Work  ")

        assertNull(blank)
        assertEquals("Work", created?.name)
        assertEquals(listOf("Work"), repository.customCategories.first().map { it.name })
    }

    @Test
    fun addCustomCategory_doesNotCreateDuplicateName() = runBlocking {
        val first = repository.addCustomCategory("Work")
        val second = repository.addCustomCategory("Work")
        val categories = repository.customCategories.first()

        assertEquals(first?.id, second?.id)
        assertEquals(1, categories.size)
        assertEquals("Work", categories.single().name)
    }

    @Test
    fun itemCanBeSavedWithCreatedCustomCategory() = runBlocking {
        val category = requireNotNull(repository.addCustomCategory("Work"))
        repository.save(
            ClothingEntity(
                name = "Jacket",
                category = ClothingCategory.OTHER,
                customCategoryName = category.name,
                purchasePrice = 5000,
                imageUri = "image"
            )
        )

        val saved = repository.items.first().single()

        assertEquals("Work", saved.customCategoryName)
        assertEquals(ClothingCategory.OTHER, saved.category)
    }
}
