package com.chibaminto.wearcost.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.chibaminto.wearcost.CategoryOption
import com.chibaminto.wearcost.FixedClothingCategories
import com.chibaminto.wearcost.R
import com.chibaminto.wearcost.closetSummary
import com.chibaminto.wearcost.WearCostTestTags
import com.chibaminto.wearcost.data.BatchWearRecordResult
import com.chibaminto.wearcost.data.ClothingCategory
import com.chibaminto.wearcost.data.ClothingEntity
import com.chibaminto.wearcost.data.TodayOutfitEditResult
import com.chibaminto.wearcost.data.TodayOutfitEditResultState
import com.chibaminto.wearcost.data.WearRecordSnapshot
import com.chibaminto.wearcost.data.todayEpochDay
import com.chibaminto.wearcost.ui.theme.WearCostTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenBatchSelectionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selectionMode_selectsAndClearsItems() {
        setHomeContent(items = testItems())

        composeRule.onNodeWithTag(WearCostTestTags.StartBatchRecord).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.SelectionHeader).assertIsDisplayed()

        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(1)).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(2)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.today_outfit_add_summary_format, 2))
            .assertIsDisplayed()

        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(1)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.today_outfit_add_summary_format, 1))
            .assertIsDisplayed()

        composeRule.onNodeWithTag(WearCostTestTags.SelectionCancel).performClick()
        composeRule.onAllNodesWithTag(WearCostTestTags.SelectionHeader).assertCountEquals(0)
        composeRule.onNodeWithTag(WearCostTestTags.AddClothingFab).assertIsDisplayed()
    }

    @Test
    fun batchRecordSuccessReturnsToNormalModeAndShowsSnackbar() {
        var items by mutableStateOf(testItems())

        setHomeContent(
            itemsProvider = { items },
            onEditTodayOutfit = { added, removed, wornDateEpochDay, onEdited ->
                val ids = added + removed
                val snapshots = items
                    .filter { it.id in ids }
                    .map { item ->
                        WearRecordSnapshot(
                            operationId = "batch-operation",
                            clothingId = item.id,
                            wearCount = item.wearCount,
                            lastWornDateEpochDay = item.lastWornDateEpochDay,
                            lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                            updatedAtMillis = item.updatedAtMillis
                        )
                    }
                items = items.map { item ->
                    if (item.id in added) {
                        item.copy(
                            wearCount = item.wearCount + 1,
                            lastWornDateEpochDay = wornDateEpochDay
                        )
                    } else {
                        item
                    }
                }
                onEdited(
                    TodayOutfitEditResultState.Success(
                        TodayOutfitEditResult(
                            operationId = "edit-operation",
                            snapshots = snapshots,
                            removedRecords = emptyList(),
                            addedIds = added,
                            removedIds = removed
                        )
                    )
                )
            }
        )

        composeRule.onNodeWithTag(WearCostTestTags.StartBatchRecord).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(1)).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(2)).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.BatchRecordButton).performClick()

        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag(WearCostTestTags.SelectionHeader).assertCountEquals(0)
        composeRule.onNodeWithTag(WearCostTestTags.AddClothingFab).assertIsDisplayed()
        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.today_outfit_added_format, 2)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.edit_today_record))
            .assertIsDisplayed()
    }

    @Test
    fun editTodayOutfitCanRemoveRecordedAndAddNewSelection() {
        val today = todayEpochDay()
        var addedIds = emptySet<Long>()
        var removedIds = emptySet<Long>()
        var items by mutableStateOf(
            listOf(
                ClothingEntity(
                    id = 1,
                    name = "Recorded top",
                    category = ClothingCategory.TOP,
                    purchasePrice = 3000,
                    wearCount = 1,
                    lastWornDateEpochDay = today
                ),
                ClothingEntity(
                    id = 2,
                    name = "Recorded bottom",
                    category = ClothingCategory.BOTTOM,
                    purchasePrice = 5000,
                    wearCount = 1,
                    lastWornDateEpochDay = today
                ),
                ClothingEntity(
                    id = 3,
                    name = "New shoes",
                    category = ClothingCategory.SHOES,
                    purchasePrice = 8000,
                    wearCount = 0
                )
            )
        )

        setHomeContent(
            itemsProvider = { items },
            onEditTodayOutfit = { added, removed, wornDateEpochDay, onEdited ->
                addedIds = added
                removedIds = removed
                val targetIds = added + removed
                val snapshots = items.filter { it.id in targetIds }.map { item ->
                    WearRecordSnapshot(
                        operationId = "edit-operation",
                        clothingId = item.id,
                        wearCount = item.wearCount,
                        lastWornDateEpochDay = item.lastWornDateEpochDay,
                        lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                        updatedAtMillis = item.updatedAtMillis
                    )
                }
                items = items.map { item ->
                    when (item.id) {
                        in added -> item.copy(wearCount = item.wearCount + 1, lastWornDateEpochDay = wornDateEpochDay)
                        in removed -> item.copy(wearCount = item.wearCount - 1, lastWornDateEpochDay = null)
                        else -> item
                    }
                }
                onEdited(
                    TodayOutfitEditResultState.Success(
                        TodayOutfitEditResult(
                            operationId = "edit-operation",
                            snapshots = snapshots,
                            removedRecords = emptyList(),
                            addedIds = added,
                            removedIds = removed
                        )
                    )
                )
            }
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.edit_today_record))
            .performClick()

        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.already_recorded_label))
            .assertCountEquals(2)
        composeRule.onNodeWithTag(WearCostTestTags.BatchRecordButton)
            .assertTextContains(composeRule.activity.getString(R.string.save_changes))
        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(1)).performClick()
        composeRule.onNodeWithTag(WearCostTestTags.BatchRecordButton)
            .assertTextContains(composeRule.activity.getString(R.string.save_changes))
        composeRule.onNodeWithTag(WearCostTestTags.HomeList)
            .performScrollToNode(hasTestTag(WearCostTestTags.clothingCard(3)))
        composeRule.onNodeWithTag(WearCostTestTags.clothingCard(3)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.today_outfit_change_summary_format, 1, 1))
            .assertIsDisplayed()
        composeRule.onNodeWithTag(WearCostTestTags.BatchRecordButton).performClick()
        composeRule.waitForIdle()

        assertEquals(setOf(3L), addedIds)
        assertEquals(setOf(1L), removedIds)
    }

    @Test
    fun todayRecordCardIsGuideBeforeRecordAndCompactAfterRecord() {
        var items by mutableStateOf(testItems())

        setHomeContent(
            itemsProvider = { items },
            onRecordWear = { id, wornDateEpochDay, onRecorded ->
                val item = items.first { it.id == id }
                onRecorded(
                    WearRecordSnapshot(
                        operationId = "single-$id",
                        clothingId = id,
                        wearCount = item.wearCount,
                        lastWornDateEpochDay = item.lastWornDateEpochDay,
                        lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                        updatedAtMillis = item.updatedAtMillis
                    )
                )
                items = items.map {
                    if (it.id == id) it.copy(wearCount = it.wearCount + 1, lastWornDateEpochDay = wornDateEpochDay)
                    else it
                }
            }
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.start_today_outfit_record))
            .assertIsDisplayed()

        composeRule.onNodeWithTag(WearCostTestTags.todayWoreButton(1)).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.edit_today_record))
            .assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.edit_today_record))
            .performClick()
        composeRule.onNodeWithTag(WearCostTestTags.SelectionHeader).assertIsDisplayed()
    }

    @Test
    fun unwornCardShowsUnknownCostAndDoesNotShowFirstWearPriceSentence() {
        setHomeContent(items = testItems())

        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.cost_per_wear_unknown))
            .assertCountEquals(2)
        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.cost_calculated_after_wear))
            .assertCountEquals(2)
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(
                R.string.first_wear_cost_format,
                "¥3,000"
            )
        ).assertCountEquals(0)
    }

    @Test
    fun listCardDoesNotShowFormulaAndUnnamedFallbackDoesNotRepeatCategory() {
        setHomeContent(
            items = listOf(
                ClothingEntity(
                    id = 1,
                    name = "",
                    category = ClothingCategory.OTHER,
                    purchasePrice = 4900,
                    wearCount = 3
                )
            )
        )

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.unnamed_item))
            .assertIsDisplayed()
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.unnamed_item_format, composeRule.activity.getString(R.string.category_other))
        ).assertCountEquals(0)
        composeRule.onAllNodesWithText(
            composeRule.activity.getString(R.string.cost_formula_format, "¥4,900", 3)
        ).assertCountEquals(0)
    }

    @Test
    fun singleRecordUpdatesCostAndUndoRestoresIt() {
        var items by mutableStateOf(
            listOf(
                ClothingEntity(
                    id = 1,
                    name = "Tops",
                    category = ClothingCategory.TOP,
                    purchasePrice = 5000,
                    wearCount = 2
                ),
                ClothingEntity(
                    id = 2,
                    name = "Bottoms",
                    category = ClothingCategory.BOTTOM,
                    purchasePrice = 3000,
                    wearCount = 1
                )
            )
        )

        setHomeContent(
            itemsProvider = { items },
            onRecordWear = { id, wornDateEpochDay, onRecorded ->
                val item = items.first { it.id == id }
                onRecorded(
                    WearRecordSnapshot(
                        operationId = "single-$id",
                        clothingId = id,
                        wearCount = item.wearCount,
                        lastWornDateEpochDay = item.lastWornDateEpochDay,
                        lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                        updatedAtMillis = item.updatedAtMillis
                    )
                )
                items = items.map {
                    if (it.id == id) it.copy(wearCount = it.wearCount + 1, lastWornDateEpochDay = wornDateEpochDay)
                    else it
                }
            },
            onUndoWear = { snapshot ->
                items = items.map {
                    if (it.id == snapshot.clothingId) {
                        it.copy(
                            wearCount = snapshot.wearCount,
                            lastWornDateEpochDay = snapshot.lastWornDateEpochDay,
                            lastWearRecordedAtMillis = snapshot.lastWearRecordedAtMillis,
                            updatedAtMillis = snapshot.updatedAtMillis
                        )
                    } else {
                        it
                    }
                }
            }
        )

        composeRule.onNodeWithTag(WearCostTestTags.todayWoreButton(1)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("¥1,667").assertIsDisplayed()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.undo)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("¥2,500").assertIsDisplayed()
    }

    @Test
    fun firstWearRecordsUpdateCostValue() {
        var items by mutableStateOf(
            listOf(
                ClothingEntity(
                    id = 1,
                    name = "New top",
                    category = ClothingCategory.TOP,
                    purchasePrice = 5000,
                    wearCount = 0
                ),
                ClothingEntity(
                    id = 2,
                    name = "Favorite pants",
                    category = ClothingCategory.BOTTOM,
                    purchasePrice = 10000,
                    wearCount = 4
                )
            )
        )

        setHomeContent(
            itemsProvider = { items },
            onRecordWear = { id, wornDateEpochDay, onRecorded ->
                val item = items.first { it.id == id }
                onRecorded(
                    WearRecordSnapshot(
                        operationId = "single-$id",
                        clothingId = id,
                        wearCount = item.wearCount,
                        lastWornDateEpochDay = item.lastWornDateEpochDay,
                        lastWearRecordedAtMillis = item.lastWearRecordedAtMillis,
                        updatedAtMillis = item.updatedAtMillis
                    )
                )
                items = items.map {
                    if (it.id == id) it.copy(wearCount = it.wearCount + 1, lastWornDateEpochDay = wornDateEpochDay)
                    else it
                }
            }
        )

        composeRule.onNodeWithTag(WearCostTestTags.todayWoreButton(1)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("¥5,000").assertIsDisplayed()

    }

    private fun setHomeContent(
        items: List<ClothingEntity> = testItems(),
        onRecordWear: (
            Long,
            Long,
            (WearRecordSnapshot?) -> Unit
        ) -> Unit = { _, _, onRecorded -> onRecorded(null) },
        onRecordWearBatch: (
            Set<Long>,
            Long,
            (BatchWearRecordResult) -> Unit
        ) -> Unit = { _, _, onRecorded -> onRecorded(BatchWearRecordResult.Success(emptyList())) },
        onEditTodayOutfit: (
            Set<Long>,
            Set<Long>,
            Long,
            (TodayOutfitEditResultState) -> Unit
        ) -> Unit = { _, _, _, onEdited ->
            onEdited(
                TodayOutfitEditResultState.Success(
                    TodayOutfitEditResult(
                        operationId = "edit",
                        snapshots = emptyList(),
                        removedRecords = emptyList(),
                        addedIds = emptySet(),
                        removedIds = emptySet()
                    )
                )
            )
        }
    ) {
        setHomeContent(
            itemsProvider = { items },
            onRecordWear = onRecordWear,
            onRecordWearBatch = onRecordWearBatch,
            onEditTodayOutfit = onEditTodayOutfit
        )
    }

    private fun setHomeContent(
        itemsProvider: () -> List<ClothingEntity>,
        onRecordWear: (
            Long,
            Long,
            (WearRecordSnapshot?) -> Unit
        ) -> Unit = { _, _, onRecorded -> onRecorded(null) },
        onRecordWearBatch: (
            Set<Long>,
            Long,
            (BatchWearRecordResult) -> Unit
        ) -> Unit = { _, _, onRecorded -> onRecorded(BatchWearRecordResult.Success(emptyList())) },
        onEditTodayOutfit: (
            Set<Long>,
            Set<Long>,
            Long,
            (TodayOutfitEditResultState) -> Unit
        ) -> Unit = { _, _, _, onEdited ->
            onEdited(
                TodayOutfitEditResultState.Success(
                    TodayOutfitEditResult(
                        operationId = "edit",
                        snapshots = emptyList(),
                        removedRecords = emptyList(),
                        addedIds = emptySet(),
                        removedIds = emptySet()
                    )
                )
            )
        },
        onUndoWear: (WearRecordSnapshot) -> Unit = {}
    ) {
        composeRule.setContent {
            val items = itemsProvider()
            WearCostTheme {
                HomeScreen(
                    items = items,
                    summary = closetSummary(items),
                    categoryOptions = FixedClothingCategories.map { CategoryOption(fixedCategory = it) },
                    currencyCode = "JPY",
                    selectedCategoryKey = null,
                    updatingWearItemIds = emptySet(),
                    onCategorySelected = {},
                    onSettings = {},
                    onAdd = {},
                    onEdit = {},
                    onRecordWear = onRecordWear,
                    onRecordWearBatch = onRecordWearBatch,
                    onEditTodayOutfit = onEditTodayOutfit,
                    onUndoWear = onUndoWear,
                    onUndoWearBatch = { _, onRestored -> onRestored(emptyList()) },
                    onUndoTodayOutfitEdit = { _, onRestored -> onRestored(emptyList()) }
                )
            }
        }
    }

    private fun testItems(): List<ClothingEntity> =
        listOf(
            ClothingEntity(
                id = 1,
                name = "Tops",
                category = ClothingCategory.TOP,
                purchasePrice = 3000,
                wearCount = 0
            ),
            ClothingEntity(
                id = 2,
                name = "Bottoms",
                category = ClothingCategory.BOTTOM,
                purchasePrice = 5000,
                wearCount = 0
            ),
            ClothingEntity(
                id = 3,
                name = "Shoes",
                category = ClothingCategory.SHOES,
                purchasePrice = 8000,
                wearCount = 0
            )
        )
}
