package com.chibaminto.wearcost.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ClothingRepository(
    private val dao: ClothingDao
) {
    val items: Flow<List<ClothingEntity>> = dao.observeItems()
    val wearHistory: Flow<List<WearHistoryEntry>> = dao.observeWearHistory()
    val customCategories: Flow<List<CustomCategoryEntity>> = dao.observeCustomCategories()
    private val wearMutexes = mutableMapOf<Long, Mutex>()

    suspend fun save(item: ClothingEntity) {
        val now = System.currentTimeMillis()
        if (item.id == 0L) {
            dao.insert(item.copy(createdAtMillis = now, updatedAtMillis = now))
        } else {
            dao.update(item.copy(updatedAtMillis = now))
        }
    }

    suspend fun delete(item: ClothingEntity) {
        dao.archiveItem(item.id)
    }

    suspend fun deleteWearHistoryEntry(clothingId: Long, wornDateEpochDay: Long) {
        mutexFor(clothingId).withLock {
            dao.deleteWearHistoryEntry(clothingId, wornDateEpochDay)
        }
    }

    suspend fun purgeArchivedItems(): Int = dao.purgeArchivedItems()

    suspend fun recordWear(id: Long, wornDateEpochDay: Long): WearRecordSnapshot? =
        mutexFor(id).withLock {
            dao.recordWearWithHistory(
                id = id,
                wornDateEpochDay = wornDateEpochDay,
                operationId = UUID.randomUUID().toString()
            )
        }

    suspend fun recordWearBatch(ids: Set<Long>, wornDateEpochDay: Long): BatchWearRecordResult {
        val uniqueIds = ids.filter { it > 0L }.distinct().sorted()
        if (uniqueIds.isEmpty()) return BatchWearRecordResult.Success(emptyList())
        return withItemLocks(uniqueIds) {
            runCatching {
                dao.recordWearBatch(
                    ids = uniqueIds,
                    wornDateEpochDay = wornDateEpochDay,
                    operationId = UUID.randomUUID().toString()
                )
            }.fold(
                onSuccess = { BatchWearRecordResult.Success(it) },
                onFailure = { BatchWearRecordResult.MissingItems }
            )
        }
    }

    suspend fun restoreWearSnapshot(snapshot: WearRecordSnapshot): List<Long> =
        mutexFor(snapshot.clothingId).withLock {
            dao.restoreWearSnapshots(listOf(snapshot))
        }

    suspend fun restoreWearSnapshots(snapshots: List<WearRecordSnapshot>): List<Long> {
        val uniqueIds = snapshots.map { it.clothingId }.distinct().sorted()
        if (uniqueIds.isEmpty()) return emptyList()
        return withItemLocks(uniqueIds) {
            dao.restoreWearSnapshots(snapshots)
        }
    }

    suspend fun editTodayOutfit(
        addedIds: Set<Long>,
        removedIds: Set<Long>,
        wornDateEpochDay: Long
    ): TodayOutfitEditResultState {
        val added = addedIds.filter { it > 0L }.distinct().sorted()
        val removed = removedIds.filter { it > 0L }.distinct().sorted()
        val allIds = (added + removed).distinct().sorted()
        if (allIds.isEmpty()) {
            return TodayOutfitEditResultState.Success(
                TodayOutfitEditResult(
                    operationId = UUID.randomUUID().toString(),
                    snapshots = emptyList(),
                    removedRecords = emptyList(),
                    addedIds = emptySet(),
                    removedIds = emptySet()
                )
            )
        }
        return withItemLocks(allIds) {
            runCatching {
                dao.editTodayOutfit(
                    addedIds = added,
                    removedIds = removed,
                    wornDateEpochDay = wornDateEpochDay,
                    operationId = UUID.randomUUID().toString()
                )
            }.fold(
                onSuccess = { TodayOutfitEditResultState.Success(it) },
                onFailure = { TodayOutfitEditResultState.MissingItems }
            )
        }
    }

    suspend fun restoreTodayOutfitEdit(result: TodayOutfitEditResult): List<Long> {
        val uniqueIds = result.snapshots.map { it.clothingId }.distinct().sorted()
        if (uniqueIds.isEmpty()) return emptyList()
        return withItemLocks(uniqueIds) {
            dao.restoreTodayOutfitEdit(result)
        }
    }

    private fun mutexFor(id: Long): Mutex =
        synchronized(wearMutexes) {
            wearMutexes.getOrPut(id) { Mutex() }
        }

    private suspend fun <T> withItemLocks(ids: List<Long>, block: suspend () -> T): T {
        suspend fun lockAt(index: Int): T =
            if (index == ids.size) {
                block()
            } else {
                mutexFor(ids[index]).withLock {
                    lockAt(index + 1)
                }
            }
        return lockAt(0)
    }

    suspend fun addCustomCategory(name: String): CustomCategoryEntity? {
        val normalizedName = name.trim()
        if (normalizedName.isEmpty()) return null

        dao.getCustomCategoryByName(normalizedName)?.let { return it }

        val now = System.currentTimeMillis()
        val category = CustomCategoryEntity(
            name = normalizedName,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        val insertedId = dao.insertCustomCategory(
            category
        )
        return if (insertedId > 0L) {
            category.copy(id = insertedId)
        } else {
            dao.getCustomCategoryByName(normalizedName)
        }
    }

    suspend fun deleteCustomCategory(category: CustomCategoryEntity) {
        dao.moveCustomCategoryItemsToOther(category.name)
        dao.deleteCustomCategory(category)
    }
}
