package com.chibaminto.wearcost

import com.chibaminto.wearcost.data.ClothingEntity
import com.chibaminto.wearcost.data.ClothingCategory
import com.chibaminto.wearcost.data.hasWearRecordedOn
import com.chibaminto.wearcost.data.costPerWear
import com.chibaminto.wearcost.data.lastWornEpochDay

enum class ClosetSort {
    RECENTLY_WORN,
    LEAST_RECENTLY_WORN,
    COST_PER_WEAR_HIGH,
    COST_PER_WEAR_LOW,
    WEAR_COUNT_HIGH,
    WEAR_COUNT_LOW,
    PURCHASE_PRICE_HIGH,
    PURCHASE_PRICE_LOW,
    NEWEST_CREATED,
    OLDEST_CREATED
}

sealed interface TodayRecordCardState {
    data object NotRecorded : TodayRecordCardState
    data class Recorded(val exactCount: Int?) : TodayRecordCardState
}

val AddEditPrimaryCategories: List<ClothingCategory> = listOf(
    ClothingCategory.TOP,
    ClothingCategory.BOTTOM,
    ClothingCategory.OUTER,
    ClothingCategory.SHOES,
    ClothingCategory.BAG,
    ClothingCategory.ACCESSORY,
    ClothingCategory.OTHER
)

data class WearCostSummary(
    val itemCount: Int = 0,
    val totalPurchaseValue: Int = 0,
    val totalWearCount: Int = 0,
    val overallCostPerWear: Int? = null,
    val unwornCount: Int = 0,
    val unwornPurchaseValue: Int = 0
)

fun closetSummary(items: List<ClothingEntity>): WearCostSummary {
    val totalPurchaseValue = items.sumOf { it.purchasePrice }
    val totalWearCount = items.sumOf { it.wearCount }
    val unwornItems = items.filter { it.wearCount == 0 }
    return WearCostSummary(
        itemCount = items.size,
        totalPurchaseValue = totalPurchaseValue,
        totalWearCount = totalWearCount,
        overallCostPerWear = costPerWear(totalPurchaseValue, totalWearCount),
        unwornCount = unwornItems.size,
        unwornPurchaseValue = unwornItems.sumOf { it.purchasePrice }
    )
}

fun todayRecordCardState(
    items: List<ClothingEntity>,
    todayEpochDay: Long,
    locallyRecordedDates: Map<Long, Long> = emptyMap()
): TodayRecordCardState {
    val recordedIds = todayRecordedClothingIds(items, todayEpochDay, locallyRecordedDates)
    return if (recordedIds.isEmpty()) {
        TodayRecordCardState.NotRecorded
    } else {
        TodayRecordCardState.Recorded(exactCount = recordedIds.size)
    }
}

fun todayRecordedClothingIds(
    items: List<ClothingEntity>,
    todayEpochDay: Long,
    locallyRecordedDates: Map<Long, Long> = emptyMap()
): Set<Long> =
    items.asSequence()
        .filter {
            hasWearRecordedOn(it, todayEpochDay) || locallyRecordedDates[it.id] == todayEpochDay
        }
        .map { it.id }
        .toSet()

fun initialNewSelectionForTodayOutfit(): Set<Long> = emptySet()

fun batchRecordTargetIds(
    newlySelectedIds: Set<Long>,
    todayRecordedIds: Set<Long>
): Set<Long> =
    newlySelectedIds - todayRecordedIds

data class TodayOutfitSelectionSync(
    val selectedIds: Set<Long>,
    val todayRecordedIds: Set<Long>
)

data class TodayOutfitEditDiff(
    val addedIds: Set<Long>,
    val removedIds: Set<Long>
) {
    val hasChanges: Boolean = addedIds.isNotEmpty() || removedIds.isNotEmpty()
    val changeCount: Int = addedIds.size + removedIds.size
}

fun initialTodayOutfitSelection(originalRecordedIds: Set<Long>): Set<Long> =
    originalRecordedIds

fun todayOutfitEditDiff(
    originalRecordedIds: Set<Long>,
    currentSelectedIds: Set<Long>
): TodayOutfitEditDiff =
    TodayOutfitEditDiff(
        addedIds = currentSelectedIds - originalRecordedIds,
        removedIds = originalRecordedIds - currentSelectedIds
    )

fun todayOutfitSelectedCount(currentSelectedIds: Set<Long>): Int =
    currentSelectedIds.size

fun syncTodayOutfitSelectionIds(
    selectedIds: Set<Long>,
    todayRecordedIds: Set<Long>,
    existingIds: Set<Long>,
    currentTodayRecordedIds: Set<Long>,
    isSelectionMode: Boolean
): TodayOutfitSelectionSync {
    val syncedTodayRecordedIds = if (isSelectionMode) {
        currentTodayRecordedIds intersect existingIds
    } else {
        todayRecordedIds intersect existingIds
    }
    return TodayOutfitSelectionSync(
        selectedIds = selectedIds intersect existingIds,
        todayRecordedIds = syncedTodayRecordedIds
    )
}

fun clothingDisplayNameOrFallback(name: String, fallback: String): String =
    name.trim().takeIf { it.isNotBlank() } ?: fallback

fun canSaveClothingDraft(
    hasImage: Boolean,
    hasPrice: Boolean,
    selectedCategoryKey: String?,
    wearCount: Int
): Boolean =
    hasImage && hasPrice && !selectedCategoryKey.isNullOrBlank() && wearCount >= 0

fun shouldShowRequiredFieldError(saveAttempted: Boolean, hasValue: Boolean): Boolean =
    saveAttempted && !hasValue

fun categoryCounts(
    items: List<ClothingEntity>,
    categoryOptions: List<CategoryOption>
): Map<String?, Int> {
    val counts = mutableMapOf<String?, Int>(null to items.size)
    categoryOptions.forEach { counts[it.key] = 0 }
    items.forEach { item ->
        val key = itemCategoryKey(item)
        counts[key] = (counts[key] ?: 0) + 1
    }
    return counts
}

fun sortClothingItems(
    items: List<ClothingEntity>,
    sort: ClosetSort
): List<ClothingEntity> =
    when (sort) {
        ClosetSort.RECENTLY_WORN -> items.sortedWith(
            compareByDescending<ClothingEntity> { lastWornEpochDay(it) ?: Long.MIN_VALUE }
                .thenByDescending { it.createdAtMillis }
                .thenBy { it.id }
        )

        ClosetSort.LEAST_RECENTLY_WORN -> items.sortedWith(
            compareBy<ClothingEntity> { lastWornEpochDay(it) ?: Long.MIN_VALUE }
                .thenBy { it.createdAtMillis }
                .thenBy { it.id }
        )

        ClosetSort.COST_PER_WEAR_HIGH -> items.sortedWith(
            compareBy<ClothingEntity> { costPerWear(it.purchasePrice, it.wearCount) == null }
                .thenByDescending { costPerWear(it.purchasePrice, it.wearCount) ?: Int.MIN_VALUE }
                .thenBy { it.id }
        )

        ClosetSort.COST_PER_WEAR_LOW -> items.sortedWith(
            compareBy<ClothingEntity> { costPerWear(it.purchasePrice, it.wearCount) == null }
                .thenBy { costPerWear(it.purchasePrice, it.wearCount) ?: Int.MAX_VALUE }
                .thenBy { it.id }
        )

        ClosetSort.WEAR_COUNT_HIGH -> items.sortedWith(
            compareByDescending<ClothingEntity> { it.wearCount }
                .thenBy { it.id }
        )

        ClosetSort.WEAR_COUNT_LOW -> items.sortedWith(
            compareBy<ClothingEntity> { it.wearCount }
                .thenBy { it.id }
        )

        ClosetSort.PURCHASE_PRICE_HIGH -> items.sortedWith(
            compareByDescending<ClothingEntity> { it.purchasePrice }
                .thenBy { it.id }
        )

        ClosetSort.PURCHASE_PRICE_LOW -> items.sortedWith(
            compareBy<ClothingEntity> { it.purchasePrice }
                .thenBy { it.id }
        )

        ClosetSort.NEWEST_CREATED -> items.sortedWith(
            compareByDescending<ClothingEntity> { it.createdAtMillis }
                .thenByDescending { it.id }
        )

        ClosetSort.OLDEST_CREATED -> items.sortedWith(
            compareBy<ClothingEntity> { it.createdAtMillis }
                .thenBy { it.id }
        )
    }

fun alreadyRecordedTodayIds(
    items: List<ClothingEntity>,
    selectedIds: Set<Long>,
    todayEpochDay: Long,
    locallyRecordedDates: Map<Long, Long> = emptyMap()
): Set<Long> =
    items.asSequence()
        .filter { it.id in selectedIds }
        .filter { hasWearRecordedOn(it, todayEpochDay) || locallyRecordedDates[it.id] == todayEpochDay }
        .map { it.id }
        .toSet()

fun batchTargetIdsExcludingAlreadyRecorded(
    selectedIds: Set<Long>,
    alreadyRecordedIds: Set<Long>
): Set<Long> =
    selectedIds - alreadyRecordedIds
