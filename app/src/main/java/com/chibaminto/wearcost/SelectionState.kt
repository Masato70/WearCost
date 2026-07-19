package com.chibaminto.wearcost

fun saveClothingIdSet(ids: Set<Long>): LongArray =
    ids.toLongArray()

fun restoreClothingIdSet(ids: LongArray): Set<Long> =
    ids.toSet()

fun removeMissingSelectedClothingIds(
    selectedIds: Set<Long>,
    existingIds: Set<Long>
): Set<Long> =
    selectedIds intersect existingIds
