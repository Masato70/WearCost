package com.chibaminto.wearcost.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wear_records",
    foreignKeys = [
        ForeignKey(
            entity = ClothingEntity::class,
            parentColumns = ["id"],
            childColumns = ["clothingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("clothingId"),
        Index(value = ["clothingId", "wornDateEpochDay"])
    ]
)
data class WearRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clothingId: Long,
    val wornDateEpochDay: Long,
    val recordedAtMillis: Long,
    val operationId: String?
)
