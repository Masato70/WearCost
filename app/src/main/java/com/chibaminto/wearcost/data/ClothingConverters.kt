package com.chibaminto.wearcost.data

import androidx.room.TypeConverter

class ClothingConverters {
    @TypeConverter
    fun categoryToString(category: ClothingCategory): String = category.name

    @TypeConverter
    fun stringToCategory(value: String): ClothingCategory =
        runCatching { ClothingCategory.valueOf(value) }.getOrDefault(ClothingCategory.OTHER)
}
