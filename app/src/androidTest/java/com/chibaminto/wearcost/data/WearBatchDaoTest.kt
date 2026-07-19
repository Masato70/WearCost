package com.chibaminto.wearcost.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearBatchDaoTest {
    private lateinit var database: WearCostDatabase
    private lateinit var dao: ClothingDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            WearCostDatabase::class.java
        ).build()
        dao = database.clothingDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recordWearBatch_incrementsAllSelectedItemsAndRestoreRevertsAll() = runBlocking {
        insertItems(1, 2, 3)

        val snapshots = dao.recordWearBatch(
            ids = listOf(1, 2, 3),
            wornDateEpochDay = 20_000,
            operationId = "batch"
        )

        assertEquals(3, snapshots.size)
        assertEquals(1, dao.getItemById(1)?.wearCount)
        assertEquals(1, dao.getItemById(2)?.wearCount)
        assertEquals(1, dao.getItemById(3)?.wearCount)
        assertEquals(20_000L, dao.getItemById(1)?.lastWornDateEpochDay)

        val missingIds = dao.restoreWearSnapshots(snapshots)

        assertEquals(emptyList<Long>(), missingIds)
        assertEquals(0, dao.getItemById(1)?.wearCount)
        assertEquals(0, dao.getItemById(2)?.wearCount)
        assertEquals(0, dao.getItemById(3)?.wearCount)
        assertNull(dao.getItemById(1)?.lastWornDateEpochDay)
    }

    @Test
    fun restoreWearSnapshots_skipsDeletedItemsAndDoesNotRecreateThem() = runBlocking {
        insertItems(1, 2, 3)
        val snapshots = dao.recordWearBatch(
            ids = listOf(1, 2, 3),
            wornDateEpochDay = 20_000,
            operationId = "batch"
        )
        dao.delete(requireNotNull(dao.getItemById(2)))

        val missingIds = dao.restoreWearSnapshots(snapshots)

        assertEquals(listOf(2L), missingIds)
        assertEquals(0, dao.getItemById(1)?.wearCount)
        assertNull(dao.getItemById(2))
        assertEquals(0, dao.getItemById(3)?.wearCount)
    }

    @Test
    fun restoreWearSnapshots_restoresOnlyWearColumnsAndKeepsUserEdits() = runBlocking {
        insertItems(1)
        val snapshots = dao.recordWearBatch(
            ids = listOf(1),
            wornDateEpochDay = 20_000,
            operationId = "batch"
        )
        val edited = requireNotNull(dao.getItemById(1)).copy(
            name = "Edited name",
            category = ClothingCategory.BOTTOM,
            purchasePrice = 9000,
            imageUri = "edited-image",
            updatedAtMillis = 99_999
        )
        dao.update(edited)

        dao.restoreWearSnapshots(snapshots)
        val restored = requireNotNull(dao.getItemById(1))

        assertEquals("Edited name", restored.name)
        assertEquals(ClothingCategory.BOTTOM, restored.category)
        assertEquals(9000, restored.purchasePrice)
        assertEquals("edited-image", restored.imageUri)
        assertEquals(0, restored.wearCount)
        assertNull(restored.lastWornDateEpochDay)
    }

    @Test
    fun recordWearBatch_missingItemFailsBeforePartialUpdate() = runBlocking {
        insertItems(1, 2)

        runCatching {
            dao.recordWearBatch(
                ids = listOf(1, 2, 99),
                wornDateEpochDay = 20_000,
                operationId = "batch"
            )
        }

        assertEquals(0, dao.getItemById(1)?.wearCount)
        assertEquals(0, dao.getItemById(2)?.wearCount)
        assertNull(dao.getItemById(1)?.lastWornDateEpochDay)
        assertNull(dao.getItemById(2)?.lastWornDateEpochDay)
    }

    @Test
    fun editTodayOutfit_addsAndRemovesInOneTransactionAndUndoRestoresBoth() = runBlocking {
        insertItems(1, 2)
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 19_999, operationId = "old")
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "today")

        val result = dao.editTodayOutfit(
            addedIds = listOf(2),
            removedIds = listOf(1),
            wornDateEpochDay = 20_000,
            operationId = "edit"
        )

        assertEquals(setOf(2L), result.addedIds)
        assertEquals(setOf(1L), result.removedIds)
        assertEquals(1, dao.getItemById(1)?.wearCount)
        assertEquals(19_999L, dao.getItemById(1)?.lastWornDateEpochDay)
        assertEquals(1, dao.getItemById(2)?.wearCount)
        assertEquals(20_000L, dao.getItemById(2)?.lastWornDateEpochDay)

        val missingIds = dao.restoreTodayOutfitEdit(result)

        assertEquals(emptyList<Long>(), missingIds)
        assertEquals(2, dao.getItemById(1)?.wearCount)
        assertEquals(20_000L, dao.getItemById(1)?.lastWornDateEpochDay)
        assertEquals(0, dao.getItemById(2)?.wearCount)
        assertNull(dao.getItemById(2)?.lastWornDateEpochDay)
    }

    @Test
    fun editTodayOutfit_missingItemFailsBeforePartialUpdate() = runBlocking {
        insertItems(1, 2)
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "today")

        runCatching {
            dao.editTodayOutfit(
                addedIds = listOf(2, 99),
                removedIds = listOf(1),
                wornDateEpochDay = 20_000,
                operationId = "edit"
            )
        }

        assertEquals(1, dao.getItemById(1)?.wearCount)
        assertEquals(0, dao.getItemById(2)?.wearCount)
        assertEquals(20_000L, dao.getItemById(1)?.lastWornDateEpochDay)
        assertNull(dao.getItemById(2)?.lastWornDateEpochDay)
    }

    @Test
    fun archiveItem_hidesFromClosetButKeepsWearHistory() = runBlocking {
        insertItems(1)
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "wear")

        dao.archiveItem(1)

        assertEquals(emptyList<ClothingEntity>(), dao.observeItems().first())
        val history = dao.observeWearHistory().first()
        assertEquals(1, history.size)
        assertEquals(1L, history.single().clothing.id)
        assertEquals(20_000L, history.single().wornDateEpochDay)
    }

    @Test
    fun deleteWearHistoryEntry_removesDateAndRecalculatesWearState() = runBlocking {
        insertItems(1)
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 19_999, operationId = "old")
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "today-1")
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "today-2")

        dao.deleteWearHistoryEntry(clothingId = 1, wornDateEpochDay = 20_000)

        val item = requireNotNull(dao.getItemById(1))
        assertEquals(1, item.wearCount)
        assertEquals(19_999L, item.lastWornDateEpochDay)
        val history = dao.observeWearHistory().first()
        assertEquals(listOf(19_999L), history.map { it.wornDateEpochDay })
    }

    @Test
    fun purgeArchivedItems_removesOnlyDeletedClothesAndTheirHistory() = runBlocking {
        insertItems(1, 2)
        dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_000, operationId = "archived")
        dao.recordWearWithHistory(id = 2, wornDateEpochDay = 20_000, operationId = "active")
        dao.archiveItem(1)

        val deletedHistoryCount = dao.purgeArchivedItems()

        assertEquals(1, deletedHistoryCount)
        assertNull(dao.getItemById(1))
        assertEquals(2L, dao.getItemById(2)?.id)
        val history = dao.observeWearHistory().first()
        assertEquals(listOf(2L), history.map { it.clothing.id })
    }

    private suspend fun insertItems(vararg ids: Long) {
        ids.forEach { id ->
            dao.insert(
                ClothingEntity(
                    id = id,
                    name = "Item $id",
                    category = ClothingCategory.TOP,
                    purchasePrice = 1000,
                    wearCount = 0,
                    createdAtMillis = id,
                    updatedAtMillis = id
                )
            )
        }
    }
}
