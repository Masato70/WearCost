package com.chibaminto.wearcost.data

data class WearRecordSnapshot(
    val operationId: String,
    val clothingId: Long,
    val wearCount: Int,
    val lastWornDateEpochDay: Long?,
    val lastWearRecordedAtMillis: Long?,
    val updatedAtMillis: Long
)
