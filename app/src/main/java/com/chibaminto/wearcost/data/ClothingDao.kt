package com.chibaminto.wearcost.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClothingDao {
    @Query("SELECT * FROM clothing_items WHERE isArchived = 0 ORDER BY updatedAtMillis DESC")
    fun observeItems(): Flow<List<ClothingEntity>>

    @Query(
        """
        SELECT wr.wornDateEpochDay AS historyWornDateEpochDay, ci.*
        FROM wear_records AS wr
        INNER JOIN clothing_items AS ci ON ci.id = wr.clothingId
        GROUP BY wr.wornDateEpochDay, wr.clothingId
        ORDER BY wr.wornDateEpochDay DESC, MAX(wr.recordedAtMillis) DESC
        """
    )
    fun observeWearHistory(): Flow<List<WearHistoryEntry>>

    @Query("SELECT * FROM clothing_items WHERE id = :id")
    suspend fun getItemById(id: Long): ClothingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ClothingEntity): Long

    @Insert
    suspend fun insertWearRecord(record: WearRecordEntity): Long

    @Insert
    suspend fun insertWearRecords(records: List<WearRecordEntity>)

    @Update
    suspend fun update(item: ClothingEntity)

    @Delete
    suspend fun delete(item: ClothingEntity)

    @Query("UPDATE clothing_items SET isArchived = 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun archiveItem(
        id: Long,
        updatedAtMillis: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT COUNT(*) FROM wear_records WHERE clothingId IN (SELECT id FROM clothing_items WHERE isArchived = 1)")
    suspend fun countArchivedWearRecords(): Int

    @Query("DELETE FROM clothing_items WHERE isArchived = 1")
    suspend fun deleteArchivedItems(): Int

    @Transaction
    suspend fun purgeArchivedItems(): Int {
        val deletedHistoryCount = countArchivedWearRecords()
        deleteArchivedItems()
        return deletedHistoryCount
    }

    @Query(
        """
        UPDATE clothing_items
        SET wearCount = wearCount + 1,
            lastWornDateEpochDay = :wornDateEpochDay,
            lastWearRecordedAtMillis = :recordedAtMillis,
            updatedAtMillis = :recordedAtMillis
        WHERE id = :id
        """
    )
    suspend fun recordWear(
        id: Long,
        wornDateEpochDay: Long,
        recordedAtMillis: Long = System.currentTimeMillis()
    ): Int

    @Query("SELECT * FROM wear_records WHERE clothingId = :clothingId AND wornDateEpochDay = :wornDateEpochDay ORDER BY recordedAtMillis DESC")
    suspend fun getWearRecordsForDate(clothingId: Long, wornDateEpochDay: Long): List<WearRecordEntity>

    @Query("SELECT * FROM wear_records WHERE clothingId = :clothingId ORDER BY wornDateEpochDay DESC, recordedAtMillis DESC, id DESC LIMIT 1")
    suspend fun getLatestWearRecord(clothingId: Long): WearRecordEntity?

    @Query("DELETE FROM wear_records WHERE clothingId = :clothingId AND wornDateEpochDay = :wornDateEpochDay")
    suspend fun deleteWearRecordsForDate(clothingId: Long, wornDateEpochDay: Long): Int

    @Query("DELETE FROM wear_records WHERE operationId = :operationId")
    suspend fun deleteWearRecordsForOperation(operationId: String): Int

    @Transaction
    suspend fun deleteWearHistoryEntry(clothingId: Long, wornDateEpochDay: Long): Boolean {
        val item = getItemById(clothingId) ?: return false
        val records = getWearRecordsForDate(clothingId, wornDateEpochDay)
        if (records.isEmpty()) return false

        val deletedCount = deleteWearRecordsForDate(clothingId, wornDateEpochDay)
        val latest = getLatestWearRecord(clothingId)
        updateWearColumns(
            id = clothingId,
            wearCount = (item.wearCount - deletedCount).coerceAtLeast(0),
            lastWornDateEpochDay = latest?.wornDateEpochDay,
            lastWearRecordedAtMillis = latest?.recordedAtMillis,
            updatedAtMillis = System.currentTimeMillis()
        )
        return true
    }

    @Query(
        """
        UPDATE clothing_items
        SET wearCount = :wearCount,
            lastWornDateEpochDay = :lastWornDateEpochDay,
            lastWearRecordedAtMillis = :lastWearRecordedAtMillis,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """
    )
    suspend fun restoreWearSnapshot(
        id: Long,
        wearCount: Int,
        lastWornDateEpochDay: Long?,
        lastWearRecordedAtMillis: Long?,
        updatedAtMillis: Long
    ): Int

    @Query(
        """
        UPDATE clothing_items
        SET wearCount = :wearCount,
            lastWornDateEpochDay = :lastWornDateEpochDay,
            lastWearRecordedAtMillis = :lastWearRecordedAtMillis,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
        """
    )
    suspend fun updateWearColumns(
        id: Long,
        wearCount: Int,
        lastWornDateEpochDay: Long?,
        lastWearRecordedAtMillis: Long?,
        updatedAtMillis: Long
    ): Int

    @Transaction
    suspend fun recordWearWithHistory(
        id: Long,
        wornDateEpochDay: Long,
        operationId: String,
        recordedAtMillis: Long = System.currentTimeMillis()
    ): WearRecordSnapshot? {
        val before = getItemById(id) ?: return null
        insertWearRecord(
            WearRecordEntity(
                clothingId = id,
                wornDateEpochDay = wornDateEpochDay,
                recordedAtMillis = recordedAtMillis,
                operationId = operationId
            )
        )
        val updatedRows = recordWear(id, wornDateEpochDay, recordedAtMillis)
        check(updatedRows == 1) { "Failed to record wear for clothing item $id" }
        return WearRecordSnapshot(
            operationId = operationId,
            clothingId = id,
            wearCount = before.wearCount,
            lastWornDateEpochDay = before.lastWornDateEpochDay,
            lastWearRecordedAtMillis = before.lastWearRecordedAtMillis,
            updatedAtMillis = before.updatedAtMillis
        )
    }

    @Transaction
    suspend fun recordWearBatch(
        ids: List<Long>,
        wornDateEpochDay: Long,
        operationId: String,
        recordedAtMillis: Long = System.currentTimeMillis()
    ): List<WearRecordSnapshot> {
        val uniqueIds = ids.distinct()
        val beforeItems = uniqueIds.map { id ->
            getItemById(id) ?: error("Clothing item $id was not found")
        }
        beforeItems.forEach { item ->
            insertWearRecord(
                WearRecordEntity(
                    clothingId = item.id,
                    wornDateEpochDay = wornDateEpochDay,
                    recordedAtMillis = recordedAtMillis,
                    operationId = operationId
                )
            )
            val updatedRows = recordWear(
                id = item.id,
                wornDateEpochDay = wornDateEpochDay,
                recordedAtMillis = recordedAtMillis
            )
            check(updatedRows == 1) { "Failed to record wear for clothing item ${item.id}" }
        }
        return beforeItems.map { item ->
            WearRecordSnapshot(
                operationId = operationId,
                clothingId = item.id,
                wearCount = item.wearCount,
                lastWornDateEpochDay = item.lastWornDateEpochDay,
                lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                updatedAtMillis = item.updatedAtMillis
            )
        }
    }

    @Transaction
    suspend fun restoreWearSnapshots(snapshots: List<WearRecordSnapshot>): List<Long> {
        snapshots.firstOrNull()?.operationId?.let { deleteWearRecordsForOperation(it) }
        val missingIds = mutableListOf<Long>()
        snapshots.forEach { snapshot ->
            val updatedRows = restoreWearSnapshot(
                id = snapshot.clothingId,
                wearCount = snapshot.wearCount,
                lastWornDateEpochDay = snapshot.lastWornDateEpochDay,
                lastWearRecordedAtMillis = snapshot.lastWearRecordedAtMillis,
                updatedAtMillis = snapshot.updatedAtMillis
            )
            if (updatedRows == 0) {
                missingIds += snapshot.clothingId
            }
        }
        return missingIds
    }

    @Transaction
    suspend fun editTodayOutfit(
        addedIds: List<Long>,
        removedIds: List<Long>,
        wornDateEpochDay: Long,
        operationId: String,
        recordedAtMillis: Long = System.currentTimeMillis()
    ): TodayOutfitEditResult {
        val allIds = (addedIds + removedIds).distinct()
        val beforeItems = allIds.map { id ->
            getItemById(id) ?: error("Clothing item $id was not found")
        }
        val snapshots = beforeItems.map { item ->
            WearRecordSnapshot(
                operationId = operationId,
                clothingId = item.id,
                wearCount = item.wearCount,
                lastWornDateEpochDay = item.lastWornDateEpochDay,
                lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                updatedAtMillis = item.updatedAtMillis
            )
        }
        val removedRecords = mutableListOf<WearRecordEntity>()

        addedIds.distinct().forEach { id ->
            insertWearRecord(
                WearRecordEntity(
                    clothingId = id,
                    wornDateEpochDay = wornDateEpochDay,
                    recordedAtMillis = recordedAtMillis,
                    operationId = operationId
                )
            )
            val updatedRows = recordWear(id, wornDateEpochDay, recordedAtMillis)
            check(updatedRows == 1) { "Failed to add wear for clothing item $id" }
        }

        removedIds.distinct().forEach { id ->
            val item = beforeItems.first { it.id == id }
            val recordsForDate = getWearRecordsForDate(id, wornDateEpochDay)
            if (recordsForDate.isNotEmpty()) {
                removedRecords += recordsForDate
                val deletedCount = deleteWearRecordsForDate(id, wornDateEpochDay)
                val latest = getLatestWearRecord(id)
                val updatedRows = updateWearColumns(
                    id = id,
                    wearCount = (item.wearCount - deletedCount).coerceAtLeast(0),
                    lastWornDateEpochDay = latest?.wornDateEpochDay,
                    lastWearRecordedAtMillis = latest?.recordedAtMillis,
                    updatedAtMillis = recordedAtMillis
                )
                check(updatedRows == 1) { "Failed to remove wear for clothing item $id" }
            }
        }

        return TodayOutfitEditResult(
            operationId = operationId,
            snapshots = snapshots,
            removedRecords = removedRecords,
            addedIds = addedIds.toSet(),
            removedIds = removedIds.toSet()
        )
    }

    @Transaction
    suspend fun restoreTodayOutfitEdit(result: TodayOutfitEditResult): List<Long> {
        deleteWearRecordsForOperation(result.operationId)
        if (result.removedRecords.isNotEmpty()) {
            insertWearRecords(result.removedRecords.map { it.copy(id = 0) })
        }
        return restoreWearSnapshots(result.snapshots)
    }

    @Query("SELECT * FROM custom_categories ORDER BY name COLLATE NOCASE ASC")
    fun observeCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Query("SELECT * FROM custom_categories WHERE name = :name LIMIT 1")
    suspend fun getCustomCategoryByName(name: String): CustomCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomCategory(category: CustomCategoryEntity): Long

    @Delete
    suspend fun deleteCustomCategory(category: CustomCategoryEntity)

    @Query("UPDATE clothing_items SET category = 'OTHER', customCategoryName = NULL, updatedAtMillis = :updatedAtMillis WHERE customCategoryName = :name")
    suspend fun moveCustomCategoryItemsToOther(
        name: String,
        updatedAtMillis: Long = System.currentTimeMillis()
    )
}
