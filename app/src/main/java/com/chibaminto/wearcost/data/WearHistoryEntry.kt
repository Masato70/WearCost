package com.chibaminto.wearcost.data

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class WearHistoryEntry(
    @ColumnInfo(name = "historyWornDateEpochDay")
    val wornDateEpochDay: Long,
    @Embedded
    val clothing: ClothingEntity
)
