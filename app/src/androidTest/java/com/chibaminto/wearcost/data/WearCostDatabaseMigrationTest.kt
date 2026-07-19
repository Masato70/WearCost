package com.chibaminto.wearcost.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearCostDatabaseMigrationTest {
    @Test
    fun migration2To3_preservesExistingClothingAndAddsNullableWearDates() {
        val databaseName = "wearcost-migration-test"
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(databaseName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(2) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit
                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) = Unit
                    }
                )
                .build()
        )

        helper.writableDatabase.apply {
            createVersion2Schema()
            insertVersion2Clothing()
            version = 2
            WearCostDatabase.MIGRATION_2_3.migrate(this)
            version = 3

            query("SELECT * FROM clothing_items WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Legacy shirt", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                assertEquals("TOP", cursor.getString(cursor.getColumnIndexOrThrow("category")))
                assertEquals(5000, cursor.getInt(cursor.getColumnIndexOrThrow("purchasePrice")))
                assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("wearCount")))
                assertNull(cursor.valueOrNull("lastWornDateEpochDay"))
                assertNull(cursor.valueOrNull("lastWearRecordedAtMillis"))
            }
        }
        helper.close()

        val migrated = Room.databaseBuilder(
            context,
            WearCostDatabase::class.java,
            databaseName
        )
            .addMigrations(WearCostDatabase.MIGRATION_2_3, WearCostDatabase.MIGRATION_3_4)
            .build()

        kotlinx.coroutines.runBlocking {
            val dao = migrated.clothingDao()
            val before = dao.getItemById(1)
            assertEquals("Legacy shirt", before?.name)
            assertEquals(5000, before?.purchasePrice)
            assertEquals(2, before?.wearCount)
            assertNull(before?.lastWornDateEpochDay)
            assertNull(before?.lastWearRecordedAtMillis)

            dao.recordWear(id = 1, wornDateEpochDay = 20_000)
            val after = dao.getItemById(1)
            assertEquals(3, after?.wearCount)
            assertEquals(20_000L, after?.lastWornDateEpochDay)
        }
        migrated.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration3To4_preservesClothingAndCreatesWearRecordForKnownLastWear() {
        val databaseName = "wearcost-migration-3-4-test"
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(databaseName)

        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(3) {
                        override fun onCreate(db: SupportSQLiteDatabase) = Unit
                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) = Unit
                    }
                )
                .build()
        )

        helper.writableDatabase.apply {
            createVersion3Schema()
            insertVersion3ClothingWithLastWear()
            version = 3
            WearCostDatabase.MIGRATION_3_4.migrate(this)
            version = 4

            query("SELECT * FROM clothing_items WHERE id = 1").use { cursor ->
                cursor.moveToFirst()
                assertEquals("Legacy jacket", cursor.getString(cursor.getColumnIndexOrThrow("name")))
                assertEquals(3, cursor.getInt(cursor.getColumnIndexOrThrow("wearCount")))
                assertEquals(20_000L, cursor.getLong(cursor.getColumnIndexOrThrow("lastWornDateEpochDay")))
            }
            query("SELECT * FROM wear_records WHERE clothingId = 1").use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals(20_000L, cursor.getLong(cursor.getColumnIndexOrThrow("wornDateEpochDay")))
                assertEquals(30_000L, cursor.getLong(cursor.getColumnIndexOrThrow("recordedAtMillis")))
            }
        }
        helper.close()

        val migrated = Room.databaseBuilder(
            context,
            WearCostDatabase::class.java,
            databaseName
        )
            .addMigrations(WearCostDatabase.MIGRATION_3_4)
            .build()

        kotlinx.coroutines.runBlocking {
            val dao = migrated.clothingDao()
            dao.recordWearWithHistory(id = 1, wornDateEpochDay = 20_001, operationId = "test")
            val after = dao.getItemById(1)
            assertEquals(4, after?.wearCount)
            assertEquals(20_001L, after?.lastWornDateEpochDay)
        }
        migrated.close()
        context.deleteDatabase(databaseName)
    }

    private fun SupportSQLiteDatabase.createVersion2Schema() {
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS clothing_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                purchasePrice INTEGER NOT NULL,
                purchaseDateMillis INTEGER,
                wearCount INTEGER NOT NULL,
                imageUri TEXT,
                customCategoryName TEXT,
                createdAtMillis INTEGER NOT NULL,
                updatedAtMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        execSQL(
            """
            CREATE TABLE IF NOT EXISTS custom_categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdAtMillis INTEGER NOT NULL,
                updatedAtMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_categories_name ON custom_categories(name)")
    }

    private fun SupportSQLiteDatabase.createVersion3Schema() {
        createVersion2Schema()
        execSQL("ALTER TABLE clothing_items ADD COLUMN lastWornDateEpochDay INTEGER")
        execSQL("ALTER TABLE clothing_items ADD COLUMN lastWearRecordedAtMillis INTEGER")
    }

    private fun SupportSQLiteDatabase.insertVersion2Clothing() {
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "Legacy shirt")
            put("category", "TOP")
            put("purchasePrice", 5000)
            putNull("purchaseDateMillis")
            put("wearCount", 2)
            putNull("imageUri")
            putNull("customCategoryName")
            put("createdAtMillis", 1000L)
            put("updatedAtMillis", 2000L)
        }
        insert("clothing_items", SQLiteDatabase.CONFLICT_REPLACE, values)
    }

    private fun SupportSQLiteDatabase.insertVersion3ClothingWithLastWear() {
        val values = ContentValues().apply {
            put("id", 1L)
            put("name", "Legacy jacket")
            put("category", "OUTER")
            put("purchasePrice", 12000)
            putNull("purchaseDateMillis")
            put("wearCount", 3)
            putNull("imageUri")
            putNull("customCategoryName")
            put("lastWornDateEpochDay", 20_000L)
            put("lastWearRecordedAtMillis", 30_000L)
            put("createdAtMillis", 1000L)
            put("updatedAtMillis", 30_000L)
        }
        insert("clothing_items", SQLiteDatabase.CONFLICT_REPLACE, values)
    }

    private fun android.database.Cursor.valueOrNull(columnName: String): Long? {
        val index = getColumnIndexOrThrow(columnName)
        return if (isNull(index)) null else getLong(index)
    }
}
