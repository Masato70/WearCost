package com.chibaminto.wearcost.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clothing_items")
data class ClothingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: ClothingCategory,
    val purchasePrice: Int,
    val purchaseDateMillis: Long? = null,
    val wearCount: Int = 0,
    val imageUri: String? = null,
    val customCategoryName: String? = null,
    val lastWornDateEpochDay: Long? = null,
    val lastWearRecordedAtMillis: Long? = null,
    val isArchived: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val costPerWear: Int?
        get() = costPerWear(purchasePrice, wearCount)
}
