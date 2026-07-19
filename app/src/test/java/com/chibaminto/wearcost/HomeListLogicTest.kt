package com.chibaminto.wearcost

import com.chibaminto.wearcost.data.ClothingCategory
import com.chibaminto.wearcost.data.ClothingEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeListLogicTest {
    @Test
    fun closetSummary_calculatesWholeClosetTotals() {
        val summary = closetSummary(
            listOf(
                item(id = 1, price = 90_000, wearCount = 2),
                item(id = 2, price = 17_500, wearCount = 1),
                item(id = 3, price = 2_000, wearCount = 0)
            )
        )

        assertEquals(3, summary.itemCount)
        assertEquals(109_500, summary.totalPurchaseValue)
        assertEquals(3, summary.totalWearCount)
        assertEquals(36_500, summary.overallCostPerWear)
        assertEquals(1, summary.unwornCount)
        assertEquals(2_000, summary.unwornPurchaseValue)
    }

    @Test
    fun closetSummary_returnsNoCostWhenTotalWearCountIsZero() {
        val summary = closetSummary(
            listOf(
                item(id = 1, price = 100_000, wearCount = 0),
                item(id = 2, price = 2_000, wearCount = 0)
            )
        )

        assertEquals(0, summary.totalWearCount)
        assertEquals(null, summary.overallCostPerWear)
        assertEquals(2, summary.unwornCount)
        assertEquals(102_000, summary.unwornPurchaseValue)
    }

    @Test
    fun closetSummary_usesTotalPriceDividedByTotalWearsWithExpensiveItem() {
        val summary = closetSummary(
            listOf(
                item(id = 1, price = 100_000, wearCount = 1),
                item(id = 2, price = 1_000, wearCount = 9)
            )
        )

        assertEquals(101_000, summary.totalPurchaseValue)
        assertEquals(10, summary.totalWearCount)
        assertEquals(10_100, summary.overallCostPerWear)
    }

    @Test
    fun closetSummary_doesNotChangeWithSelectedCategory() {
        val items = listOf(
            item(id = 1, category = ClothingCategory.TOP, price = 8_000, wearCount = 4),
            item(id = 2, category = ClothingCategory.SHOES, price = 12_000, wearCount = 2)
        )
        val expected = closetSummary(items)

        FixedClothingCategories.forEach { selectedCategory ->
            val visibleItems = items.filter { it.category == selectedCategory }
            assertEquals(expected, closetSummary(items))
            assertTrue(visibleItems.all { it.category == selectedCategory })
        }
    }

    @Test
    fun requiredFieldError_isShownOnlyAfterSaveAttempt() {
        assertFalse(shouldShowRequiredFieldError(saveAttempted = false, hasValue = false))
        assertFalse(shouldShowRequiredFieldError(saveAttempted = true, hasValue = true))
        assertTrue(shouldShowRequiredFieldError(saveAttempted = true, hasValue = false))
    }

    @Test
    fun addEditPrimaryCategories_containsEveryFixedCategory() {
        assertEquals(FixedClothingCategories, AddEditPrimaryCategories)
    }

    @Test
    fun categoryCounts_countsAllAndEachCategory() {
        val items = listOf(
            item(id = 1, category = ClothingCategory.TOP),
            item(id = 2, category = ClothingCategory.TOP),
            item(id = 3, category = ClothingCategory.BOTTOM)
        )
        val counts = categoryCounts(items, fixedCategoryOptionsForTest())

        assertEquals(3, counts[null])
        assertEquals(2, counts[fixedCategoryKey(ClothingCategory.TOP)])
        assertEquals(1, counts[fixedCategoryKey(ClothingCategory.BOTTOM)])
        assertEquals(0, counts[fixedCategoryKey(ClothingCategory.OUTER)])
    }

    @Test
    fun sortClothingItems_sortsByEverySupportedOrder() {
        val items = listOf(
            item(id = 1, price = 9000, wearCount = 3, lastWorn = 10, created = 100),
            item(id = 2, price = 3000, wearCount = 10, lastWorn = 12, created = 300),
            item(id = 3, price = 6000, wearCount = 0, lastWorn = null, created = 200)
        )

        assertEquals(listOf(2L, 1L, 3L), sortClothingItems(items, ClosetSort.RECENTLY_WORN).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), sortClothingItems(items, ClosetSort.LEAST_RECENTLY_WORN).map { it.id })
        assertEquals(listOf(1L, 2L, 3L), sortClothingItems(items, ClosetSort.COST_PER_WEAR_HIGH).map { it.id })
        assertEquals(listOf(2L, 1L, 3L), sortClothingItems(items, ClosetSort.COST_PER_WEAR_LOW).map { it.id })
        assertEquals(listOf(2L, 1L, 3L), sortClothingItems(items, ClosetSort.WEAR_COUNT_HIGH).map { it.id })
        assertEquals(listOf(3L, 1L, 2L), sortClothingItems(items, ClosetSort.WEAR_COUNT_LOW).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), sortClothingItems(items, ClosetSort.PURCHASE_PRICE_HIGH).map { it.id })
        assertEquals(listOf(2L, 3L, 1L), sortClothingItems(items, ClosetSort.PURCHASE_PRICE_LOW).map { it.id })
        assertEquals(listOf(2L, 3L, 1L), sortClothingItems(items, ClosetSort.NEWEST_CREATED).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), sortClothingItems(items, ClosetSort.OLDEST_CREATED).map { it.id })
    }

    @Test
    fun alreadyRecordedTodayIds_detectsRecordedAndLeavesNextDayAvailable() {
        val today = 20_000L
        val items = listOf(
            item(id = 1, lastWorn = today),
            item(id = 2, lastWorn = today - 1),
            item(id = 3, lastWorn = null)
        )

        assertEquals(setOf(1L), alreadyRecordedTodayIds(items, setOf(1L, 2L, 3L), today))
        assertEquals(emptySet<Long>(), alreadyRecordedTodayIds(items, setOf(1L), today + 1))
    }

    @Test
    fun todayRecordedClothingIds_returnsOnlyItemsRecordedOnLocalToday() {
        val items = listOf(
            item(id = 1, lastWorn = 20_000),
            item(id = 2, lastWorn = 19_999),
            item(id = 3, lastWorn = null)
        )

        assertEquals(setOf(1L), todayRecordedClothingIds(items, todayEpochDay = 20_000))
        assertEquals(emptySet<Long>(), todayRecordedClothingIds(items, todayEpochDay = 20_001))
    }

    @Test
    fun todayRecordedClothingIds_rebuildsFromRoomLastWornDateAfterRecreation() {
        val items = listOf(
            item(id = 1, lastWorn = 20_000),
            item(id = 2, lastWorn = null)
        )

        assertEquals(
            setOf(1L),
            todayRecordedClothingIds(
                items = items,
                todayEpochDay = 20_000,
                locallyRecordedDates = emptyMap()
            )
        )
    }

    @Test
    fun todayRecordedClothingIds_deduplicatesRoomAndTemporaryRecordedDates() {
        val items = listOf(item(id = 1, lastWorn = 20_000))

        assertEquals(
            setOf(1L),
            todayRecordedClothingIds(
                items = items,
                todayEpochDay = 20_000,
                locallyRecordedDates = mapOf(1L to 20_000)
            )
        )
    }

    @Test
    fun todayRecordedClothingIds_excludesItemAfterUndoRestoresRoomState() {
        val items = listOf(item(id = 1, lastWorn = null))

        assertEquals(
            emptySet<Long>(),
            todayRecordedClothingIds(
                items = items,
                todayEpochDay = 20_000,
                locallyRecordedDates = emptyMap()
            )
        )
    }

    @Test
    fun todayRecordedClothingIds_excludesPreviousDayAfterLocalDateChanges() {
        val items = listOf(item(id = 1, lastWorn = 20_000))

        assertEquals(
            emptySet<Long>(),
            todayRecordedClothingIds(items = items, todayEpochDay = 20_001)
        )
    }

    @Test
    fun todayRecordCardState_returnsGuideWhenNothingRecordedToday() {
        val state = todayRecordCardState(
            items = listOf(item(id = 1, lastWorn = 19_999)),
            todayEpochDay = 20_000
        )

        assertEquals(TodayRecordCardState.NotRecorded, state)
    }

    @Test
    fun todayRecordCardState_returnsCompactRecordedStateWithExactDistinctCount() {
        val state = todayRecordCardState(
            items = listOf(
                item(id = 1, lastWorn = 20_000),
                item(id = 2, lastWorn = 20_000),
                item(id = 3, lastWorn = 19_999)
            ),
            todayEpochDay = 20_000
        )

        assertEquals(TodayRecordCardState.Recorded(exactCount = 2), state)
    }

    @Test
    fun todayRecordCardState_usesLocalRecordedDatesWithoutGuessingDuplicateCounts() {
        val state = todayRecordCardState(
            items = listOf(item(id = 1, lastWorn = null)),
            todayEpochDay = 20_000,
            locallyRecordedDates = mapOf(1L to 20_000)
        )

        assertEquals(TodayRecordCardState.Recorded(exactCount = 1), state)
    }

    @Test
    fun canSaveClothingDraft_requiresExplicitCategorySelection() {
        assertFalse(
            canSaveClothingDraft(
                hasImage = true,
                hasPrice = true,
                selectedCategoryKey = null,
                wearCount = 0
            )
        )
        assertFalse(
            canSaveClothingDraft(
                hasImage = true,
                hasPrice = true,
                selectedCategoryKey = "",
                wearCount = 0
            )
        )
    }

    @Test
    fun canSaveClothingDraft_allowsExplicitOtherCategory() {
        assertTrue(
            canSaveClothingDraft(
                hasImage = true,
                hasPrice = true,
                selectedCategoryKey = fixedCategoryKey(ClothingCategory.OTHER),
                wearCount = 0
            )
        )
    }

    @Test
    fun excludingAlreadyRecorded_targetsOnlyUnrecordedItems() {
        assertEquals(
            setOf(2L, 3L),
            batchTargetIdsExcludingAlreadyRecorded(setOf(1L, 2L, 3L), setOf(1L))
        )
    }

    @Test
    fun batchRecordTargetIds_excludesAlreadyRecordedOutfitItems() {
        assertEquals(
            setOf(3L),
            batchRecordTargetIds(
                newlySelectedIds = setOf(1L, 2L, 3L),
                todayRecordedIds = setOf(1L, 2L)
            )
        )
    }

    @Test
    fun syncTodayOutfitSelectionIds_removesDeletedIdsFromBothStates() {
        val result = syncTodayOutfitSelectionIds(
            selectedIds = setOf(1L, 2L, 3L),
            todayRecordedIds = setOf(4L, 5L),
            existingIds = setOf(1L, 4L),
            currentTodayRecordedIds = setOf(4L),
            isSelectionMode = true
        )

        assertEquals(setOf(1L), result.selectedIds)
        assertEquals(setOf(4L), result.todayRecordedIds)
    }

    @Test
    fun syncTodayOutfitSelectionIds_tracksRoomRecordedIdsWithoutClearingNewSelection() {
        val result = syncTodayOutfitSelectionIds(
            selectedIds = setOf(3L),
            todayRecordedIds = setOf(1L),
            existingIds = setOf(1L, 2L, 3L),
            currentTodayRecordedIds = setOf(1L, 2L),
            isSelectionMode = true
        )

        assertEquals(setOf(3L), result.selectedIds)
        assertEquals(setOf(1L, 2L), result.todayRecordedIds)
    }

    @Test
    fun syncTodayOutfitSelectionIds_keepsCurrentSelectionWhenRoomRecordedIdsChange() {
        val result = syncTodayOutfitSelectionIds(
            selectedIds = setOf(2L, 3L),
            todayRecordedIds = setOf(1L),
            existingIds = setOf(1L, 2L, 3L),
            currentTodayRecordedIds = setOf(1L, 2L),
            isSelectionMode = true
        )

        assertEquals(setOf(2L, 3L), result.selectedIds)
        assertEquals(setOf(1L, 2L), result.todayRecordedIds)
    }

    @Test
    fun syncTodayOutfitSelectionIds_removesRecordedIdAfterUndoOrDateChange() {
        val result = syncTodayOutfitSelectionIds(
            selectedIds = setOf(3L),
            todayRecordedIds = setOf(1L, 2L),
            existingIds = setOf(1L, 2L, 3L),
            currentTodayRecordedIds = emptySet(),
            isSelectionMode = true
        )

        assertEquals(setOf(3L), result.selectedIds)
        assertEquals(emptySet<Long>(), result.todayRecordedIds)
    }

    @Test
    fun initialNewSelectionForTodayOutfit_isEmptyEvenWhenTodayItemsExist() {
        assertEquals(emptySet<Long>(), initialNewSelectionForTodayOutfit())
    }

    @Test
    fun initialTodayOutfitSelection_usesOriginalRecordedIds() {
        assertEquals(setOf(1L, 2L), initialTodayOutfitSelection(setOf(1L, 2L)))
    }

    @Test
    fun todayOutfitEditDiff_tracksAddedRemovedAndNoChanges() {
        val original = setOf(1L, 2L)

        assertEquals(
            TodayOutfitEditDiff(addedIds = emptySet(), removedIds = setOf(2L)),
            todayOutfitEditDiff(originalRecordedIds = original, currentSelectedIds = setOf(1L))
        )
        assertEquals(
            TodayOutfitEditDiff(addedIds = emptySet(), removedIds = emptySet()),
            todayOutfitEditDiff(originalRecordedIds = original, currentSelectedIds = setOf(1L, 2L))
        )
        assertEquals(
            TodayOutfitEditDiff(addedIds = setOf(3L), removedIds = emptySet()),
            todayOutfitEditDiff(originalRecordedIds = original, currentSelectedIds = setOf(1L, 2L, 3L))
        )
        assertEquals(
            TodayOutfitEditDiff(addedIds = setOf(3L), removedIds = setOf(2L)),
            todayOutfitEditDiff(originalRecordedIds = original, currentSelectedIds = setOf(1L, 3L))
        )
    }

    @Test
    fun todayOutfitSelectedCount_countsCurrentSelectionTotal() {
        assertEquals(3, todayOutfitSelectedCount(setOf(1L, 2L, 3L)))
    }

    @Test
    fun clothingDisplayNameOrFallback_doesNotRepeatCategoryWhenNameIsMissing() {
        assertEquals("名前未設定", clothingDisplayNameOrFallback("", "名前未設定"))
        assertEquals("White shirt", clothingDisplayNameOrFallback(" White shirt ", "名前未設定"))
    }

    @Test
    fun selectedClothingIdsRemainCorrectWhenListOrderChanges() {
        val selectedIds = setOf(1L, 3L)
        val sorted = sortClothingItems(
            listOf(
                item(id = 1, price = 9000),
                item(id = 2, price = 1000),
                item(id = 3, price = 5000)
            ),
            ClosetSort.PURCHASE_PRICE_LOW
        )

        assertTrue(sorted.first { it.id == 1L }.id in selectedIds)
        assertTrue(sorted.first { it.id == 3L }.id in selectedIds)
        assertFalse(sorted.first { it.id == 2L }.id in selectedIds)
    }

    @Test
    fun selectedClothingIdsSaverRoundTripsThroughLongArray() {
        val selectedIds = setOf(3L, 1L, 2L)

        assertEquals(selectedIds, restoreClothingIdSet(saveClothingIdSet(selectedIds)))
    }

    @Test
    fun removeMissingSelectedClothingIds_removesDeletedButKeepsHiddenByCategory() {
        val selectedIds = setOf(1L, 2L, 3L)
        val allExistingIds = setOf(1L, 3L, 4L)
        val currentlyVisibleIds = setOf(1L)

        assertEquals(setOf(1L, 3L), removeMissingSelectedClothingIds(selectedIds, allExistingIds))
        assertEquals(setOf(1L, 3L), removeMissingSelectedClothingIds(selectedIds, allExistingIds))
        assertTrue(3L !in currentlyVisibleIds)
    }

    private fun item(
        id: Long,
        category: ClothingCategory = ClothingCategory.TOP,
        price: Int = 1000,
        wearCount: Int = 0,
        lastWorn: Long? = null,
        created: Long = id
    ): ClothingEntity =
        ClothingEntity(
            id = id,
            name = "Item $id",
            category = category,
            purchasePrice = price,
            wearCount = wearCount,
            lastWornDateEpochDay = lastWorn,
            createdAtMillis = created,
            updatedAtMillis = created
        )

    private fun fixedCategoryOptionsForTest(): List<CategoryOption> =
        FixedClothingCategories.map { CategoryOption(fixedCategory = it) }
}
