package com.chibaminto.wearcost.data

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.roundToInt

private const val DAY_MILLIS = 86_400_000L

fun costPerWear(purchasePrice: Int, wearCount: Int): Int? =
    if (wearCount > 0) (purchasePrice.toDouble() / wearCount).roundToInt() else null

data class WearCostChange(
    val clothingId: Long,
    val previousWearCount: Int,
    val newWearCount: Int,
    val previousCostPerWear: Int?,
    val newCostPerWear: Int?,
    val savedPerWear: Int?,
    val isFirstWear: Boolean,
    val reachedMilestone: Int?
)

private val WearMilestoneCounts = setOf(1, 5, 10, 20, 30, 50, 100)

fun reachedWearMilestone(wearCount: Int): Int? =
    wearCount.takeIf { it in WearMilestoneCounts }

fun wearCostChange(
    clothingId: Long,
    purchasePrice: Int,
    previousWearCount: Int,
    addedWearCount: Int = 1
): WearCostChange {
    val newWearCount = previousWearCount + addedWearCount
    val previousCostPerWear = costPerWear(purchasePrice, previousWearCount)
    val newCostPerWear = costPerWear(purchasePrice, newWearCount)
    val savedPerWear = if (previousCostPerWear != null && newCostPerWear != null) {
        (previousCostPerWear - newCostPerWear).coerceAtLeast(0)
    } else {
        null
    }
    return WearCostChange(
        clothingId = clothingId,
        previousWearCount = previousWearCount,
        newWearCount = newWearCount,
        previousCostPerWear = previousCostPerWear,
        newCostPerWear = newCostPerWear,
        savedPerWear = savedPerWear,
        isFirstWear = previousWearCount == 0 && newWearCount == 1,
        reachedMilestone = reachedWearMilestone(newWearCount)
    )
}

fun wearCostChangeForSnapshot(
    item: ClothingEntity,
    snapshot: WearRecordSnapshot
): WearCostChange =
    wearCostChange(
        clothingId = snapshot.clothingId,
        purchasePrice = item.purchasePrice,
        previousWearCount = snapshot.wearCount
    )

fun wearCostChangesForSnapshots(
    items: List<ClothingEntity>,
    snapshots: List<WearRecordSnapshot>
): Map<Long, WearCostChange> {
    if (snapshots.isEmpty()) return emptyMap()
    val itemsById = items.associateBy { it.id }
    return snapshots.mapNotNull { snapshot ->
        itemsById[snapshot.clothingId]?.let { item ->
            snapshot.clothingId to wearCostChangeForSnapshot(item, snapshot)
        }
    }.toMap()
}

fun removeWearCostChangesForSnapshots(
    changes: Map<Long, WearCostChange>,
    snapshots: List<WearRecordSnapshot>
): Map<Long, WearCostChange> {
    val ids = snapshots.map { it.clothingId }.toSet()
    return changes - ids
}

fun todayEpochDay(): Long =
    epochDayFromMillis(System.currentTimeMillis())

fun epochDayFromMillis(
    millis: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    val calendar = Calendar.getInstance(timeZone).apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return Math.floorDiv(calendar.timeInMillis, DAY_MILLIS)
}

/** Returns a stable daytime instant that formats back to the local date represented by [epochDay]. */
fun displayMillisFromEpochDay(
    epochDay: Long,
    timeZone: TimeZone = TimeZone.getDefault()
): Long {
    var candidateMillis = epochDay * DAY_MILLIS + DAY_MILLIS / 2
    repeat(2) {
        val candidateEpochDay = epochDayFromMillis(candidateMillis, timeZone)
        candidateMillis += (epochDay - candidateEpochDay) * DAY_MILLIS
    }
    return candidateMillis
}

fun lastWornEpochDay(item: ClothingEntity): Long? =
    item.lastWornDateEpochDay

fun hasWearRecordedOn(item: ClothingEntity, epochDay: Long): Boolean =
    lastWornEpochDay(item) == epochDay

fun relativeLastWornText(lastWornEpochDay: Long?, todayEpochDay: Long): LastWornDistance =
    when {
        lastWornEpochDay == null -> LastWornDistance.NotWorn
        lastWornEpochDay >= todayEpochDay -> LastWornDistance.Today
        todayEpochDay - lastWornEpochDay == 1L -> LastWornDistance.Yesterday
        todayEpochDay - lastWornEpochDay < 14L -> LastWornDistance.DaysAgo((todayEpochDay - lastWornEpochDay).toInt())
        else -> LastWornDistance.WeeksAgo(((todayEpochDay - lastWornEpochDay) / 7).toInt())
    }

sealed interface LastWornDistance {
    data object NotWorn : LastWornDistance
    data object Today : LastWornDistance
    data object Yesterday : LastWornDistance
    data class DaysAgo(val days: Int) : LastWornDistance
    data class WeeksAgo(val weeks: Int) : LastWornDistance
}
