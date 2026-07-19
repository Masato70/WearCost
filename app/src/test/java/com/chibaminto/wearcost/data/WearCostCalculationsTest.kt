package com.chibaminto.wearcost.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class WearCostCalculationsTest {
    @Test
    fun displayMillisFromEpochDay_restoresTokyoLocalDateWithoutPreviousDayShift() {
        val timeZone = TimeZone.getTimeZone("Asia/Tokyo")
        val original = Calendar.getInstance(timeZone).apply {
            clear()
            set(2026, Calendar.JULY, 19, 0, 0, 0)
        }
        val epochDay = epochDayFromMillis(original.timeInMillis, timeZone)
        val restored = Calendar.getInstance(timeZone).apply {
            timeInMillis = displayMillisFromEpochDay(epochDay, timeZone)
        }

        assertEquals(2026, restored.get(Calendar.YEAR))
        assertEquals(Calendar.JULY, restored.get(Calendar.MONTH))
        assertEquals(19, restored.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun displayMillisFromEpochDay_restoresNegativeOffsetLocalDate() {
        val timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        val original = Calendar.getInstance(timeZone).apply {
            clear()
            set(2026, Calendar.JULY, 19, 0, 0, 0)
        }
        val epochDay = epochDayFromMillis(original.timeInMillis, timeZone)
        val restored = Calendar.getInstance(timeZone).apply {
            timeInMillis = displayMillisFromEpochDay(epochDay, timeZone)
        }

        assertEquals(2026, restored.get(Calendar.YEAR))
        assertEquals(Calendar.JULY, restored.get(Calendar.MONTH))
        assertEquals(19, restored.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun costPerWear_returnsRoundedCostWhenWorn() {
        assertEquals(2500, costPerWear(purchasePrice = 5000, wearCount = 2))
    }

    @Test
    fun costPerWear_returnsNullWhenWearCountIsZero() {
        assertNull(costPerWear(purchasePrice = 5000, wearCount = 0))
    }

    @Test
    fun wearCostChange_calculatesSavedPerWearFromTwoToThreeWears() {
        val change = wearCostChange(
            clothingId = 1,
            purchasePrice = 5000,
            previousWearCount = 2
        )

        assertEquals(2, change.previousWearCount)
        assertEquals(3, change.newWearCount)
        assertEquals(2500, change.previousCostPerWear)
        assertEquals(1667, change.newCostPerWear)
        assertEquals(833, change.savedPerWear)
        assertFalse(change.isFirstWear)
    }

    @Test
    fun wearCostChange_firstWearHasNoPreviousCost() {
        val change = wearCostChange(
            clothingId = 1,
            purchasePrice = 5000,
            previousWearCount = 0
        )

        assertNull(change.previousCostPerWear)
        assertEquals(5000, change.newCostPerWear)
        assertNull(change.savedPerWear)
        assertTrue(change.isFirstWear)
    }

    @Test
    fun reachedWearMilestone_returnsConfiguredMilestones() {
        listOf(1, 5, 10, 20, 30, 50, 100).forEach { count ->
            assertEquals(count, reachedWearMilestone(count))
        }
    }

    @Test
    fun reachedWearMilestone_ignoresNonMilestones() {
        listOf(0, 2, 4, 6, 11, 49, 101).forEach { count ->
            assertNull(reachedWearMilestone(count))
        }
    }

    @Test
    fun removeWearCostChangesForSnapshots_clearsUndoTargetsOnly() {
        val first = wearCostChange(clothingId = 1, purchasePrice = 5000, previousWearCount = 2)
        val second = wearCostChange(clothingId = 2, purchasePrice = 3000, previousWearCount = 1)
        val remaining = removeWearCostChangesForSnapshots(
            changes = mapOf(1L to first, 2L to second),
            snapshots = listOf(snapshot(clothingId = 1, wearCount = 2))
        )

        assertFalse(remaining.containsKey(1))
        assertTrue(remaining.containsKey(2))
    }

    @Test
    fun wearCostChangesForSnapshots_calculatesEachBatchItem() {
        val items = listOf(
            item(id = 1, purchasePrice = 5000, wearCount = 3),
            item(id = 2, purchasePrice = 3000, wearCount = 2)
        )
        val changes = wearCostChangesForSnapshots(
            items = items,
            snapshots = listOf(
                snapshot(clothingId = 1, wearCount = 2),
                snapshot(clothingId = 2, wearCount = 1)
            )
        )

        assertEquals(833, changes.getValue(1).savedPerWear)
        assertEquals(1500, changes.getValue(2).savedPerWear)
    }

    @Test
    fun wearCostChangesForSnapshots_returnsNoReactionForFailureOrCancel() {
        assertTrue(wearCostChangesForSnapshots(items = listOf(item()), snapshots = emptyList()).isEmpty())
    }

    @Test
    fun hasWearRecordedOn_usesLastWornDateForDuplicatePrevention() {
        val item = ClothingEntity(
            id = 1,
            name = "T-shirt",
            category = ClothingCategory.TOP,
            purchasePrice = 5000,
            wearCount = 2,
            lastWornDateEpochDay = 20_000
        )

        assertTrue(hasWearRecordedOn(item, 20_000))
        assertFalse(hasWearRecordedOn(item, 20_001))
    }

    @Test
    fun lastWornEpochDay_doesNotInferUnknownLegacyDateFromUpdatedAt() {
        val item = ClothingEntity(
            id = 1,
            name = "Edited item",
            category = ClothingCategory.TOP,
            purchasePrice = 5000,
            wearCount = 3,
            lastWornDateEpochDay = null,
            updatedAtMillis = 1_800_000_000_000
        )

        assertNull(lastWornEpochDay(item))
    }

    @Test
    fun epochDayFromMillis_usesLocalTimeZoneAroundJapanMidnight() {
        val tokyo = TimeZone.getTimeZone("Asia/Tokyo")
        val beforeMidnight = millisInZone(2026, Calendar.JULY, 10, 23, 59, tokyo)
        val afterMidnight = millisInZone(2026, Calendar.JULY, 11, 0, 1, tokyo)

        val beforeEpochDay = epochDayFromMillis(beforeMidnight, tokyo)
        val afterEpochDay = epochDayFromMillis(afterMidnight, tokyo)

        assertEquals(beforeEpochDay + 1, afterEpochDay)
    }

    @Test
    fun hasWearRecordedOn_treatsSameLocalDateAsDuplicateAndNextDateAsAvailable() {
        val tokyo = TimeZone.getTimeZone("Asia/Tokyo")
        val recordedEpochDay = epochDayFromMillis(
            millisInZone(2026, Calendar.JULY, 11, 0, 1, tokyo),
            tokyo
        )
        val item = ClothingEntity(
            id = 1,
            name = "T-shirt",
            category = ClothingCategory.TOP,
            purchasePrice = 5000,
            wearCount = 1,
            lastWornDateEpochDay = recordedEpochDay
        )

        assertTrue(hasWearRecordedOn(item, recordedEpochDay))
        assertFalse(hasWearRecordedOn(item, recordedEpochDay + 1))
    }

    @Test
    fun relativeLastWornText_returnsExpectedBoundaries() {
        val today = 20_000L

        assertEquals(LastWornDistance.NotWorn, relativeLastWornText(null, today))
        assertEquals(LastWornDistance.Today, relativeLastWornText(today, today))
        assertEquals(LastWornDistance.Yesterday, relativeLastWornText(today - 1, today))
        assertEquals(LastWornDistance.DaysAgo(3), relativeLastWornText(today - 3, today))
        assertEquals(LastWornDistance.WeeksAgo(2), relativeLastWornText(today - 14, today))
    }

    private fun millisInZone(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        timeZone: TimeZone
    ): Long =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis

    private fun item(
        id: Long = 1,
        purchasePrice: Int = 5000,
        wearCount: Int = 0
    ): ClothingEntity =
        ClothingEntity(
            id = id,
            name = "Item $id",
            category = ClothingCategory.TOP,
            purchasePrice = purchasePrice,
            wearCount = wearCount
        )

    private fun snapshot(
        clothingId: Long,
        wearCount: Int
    ): WearRecordSnapshot =
        WearRecordSnapshot(
            operationId = "operation-$clothingId",
            clothingId = clothingId,
            wearCount = wearCount,
            lastWornDateEpochDay = null,
            lastWearRecordedAtMillis = null,
            updatedAtMillis = 0
        )
}
