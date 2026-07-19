package com.chibaminto.wearcost.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ClothingEntity::class, CustomCategoryEntity::class, WearRecordEntity::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(ClothingConverters::class)
abstract class WearCostDatabase : RoomDatabase() {
    abstract fun clothingDao(): ClothingDao

    companion object {
        @Volatile
        private var instance: WearCostDatabase? = null

        fun getInstance(context: Context): WearCostDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WearCostDatabase::class.java,
                    "wearcost.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN customCategoryName TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS custom_categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_custom_categories_name ON custom_categories(name)"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN lastWornDateEpochDay INTEGER")
                db.execSQL("ALTER TABLE clothing_items ADD COLUMN lastWearRecordedAtMillis INTEGER")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS wear_records (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        clothingId INTEGER NOT NULL,
                        wornDateEpochDay INTEGER NOT NULL,
                        recordedAtMillis INTEGER NOT NULL,
                        operationId TEXT,
                        FOREIGN KEY(clothingId) REFERENCES clothing_items(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_wear_records_clothingId ON wear_records(clothingId)")
                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS index_wear_records_clothingId_wornDateEpochDay
                    ON wear_records(clothingId, wornDateEpochDay)
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO wear_records (clothingId, wornDateEpochDay, recordedAtMillis, operationId)
                    SELECT id,
                           lastWornDateEpochDay,
                           COALESCE(lastWearRecordedAtMillis, updatedAtMillis),
                           'migration-3-4'
                    FROM clothing_items
                    WHERE wearCount > 0 AND lastWornDateEpochDay IS NOT NULL
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE clothing_items ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
