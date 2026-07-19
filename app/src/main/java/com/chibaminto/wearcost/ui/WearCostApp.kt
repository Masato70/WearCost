package com.chibaminto.wearcost.ui

import android.animation.ValueAnimator
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import coil.compose.AsyncImage
import com.chibaminto.wearcost.R
import com.chibaminto.wearcost.CategoryOption
import com.chibaminto.wearcost.ClosetSort
import com.chibaminto.wearcost.AddEditPrimaryCategories
import com.chibaminto.wearcost.TodayRecordCardState
import com.chibaminto.wearcost.canSaveClothingDraft
import com.chibaminto.wearcost.clothingDisplayNameOrFallback
import com.chibaminto.wearcost.closetSummary
import com.chibaminto.wearcost.restoreClothingIdSet
import com.chibaminto.wearcost.saveClothingIdSet
import com.chibaminto.wearcost.WearCostSummary
import com.chibaminto.wearcost.WearCostTestTags
import com.chibaminto.wearcost.WearCostViewModel
import com.chibaminto.wearcost.WearUndoState
import com.chibaminto.wearcost.alreadyRecordedTodayIds
import com.chibaminto.wearcost.batchTargetIdsExcludingAlreadyRecorded
import com.chibaminto.wearcost.categoryCounts
import com.chibaminto.wearcost.shouldUndoWear
import com.chibaminto.wearcost.shouldShowRequiredFieldError
import com.chibaminto.wearcost.FixedClothingCategories
import com.chibaminto.wearcost.batchRecordTargetIds
import com.chibaminto.wearcost.customCategoryKey
import com.chibaminto.wearcost.initialTodayOutfitSelection
import com.chibaminto.wearcost.data.BatchWearRecordResult
import com.chibaminto.wearcost.data.ClothingCategory
import com.chibaminto.wearcost.data.ClothingEntity
import com.chibaminto.wearcost.data.CustomCategoryEntity
import com.chibaminto.wearcost.data.LastWornDistance
import com.chibaminto.wearcost.data.SupportedCurrencyCodes
import com.chibaminto.wearcost.data.TodayOutfitEditResult
import com.chibaminto.wearcost.data.TodayOutfitEditResultState
import com.chibaminto.wearcost.data.WearRecordSnapshot
import com.chibaminto.wearcost.data.WearCostChange
import com.chibaminto.wearcost.data.costPerWear
import com.chibaminto.wearcost.data.hasWearRecordedOn
import com.chibaminto.wearcost.data.lastWornEpochDay
import com.chibaminto.wearcost.data.relativeLastWornText
import com.chibaminto.wearcost.data.todayEpochDay
import com.chibaminto.wearcost.data.wearCostChangeForSnapshot
import com.chibaminto.wearcost.data.wearCostChangesForSnapshots
import com.chibaminto.wearcost.fixedCategoryKey
import com.chibaminto.wearcost.initialNewSelectionForTodayOutfit
import com.chibaminto.wearcost.itemCategoryKey
import com.chibaminto.wearcost.sortClothingItems
import com.chibaminto.wearcost.syncTodayOutfitSelectionIds
import com.chibaminto.wearcost.todayOutfitEditDiff
import com.chibaminto.wearcost.todayOutfitSelectedCount
import com.chibaminto.wearcost.todayRecordedClothingIds
import com.chibaminto.wearcost.todayRecordCardState
import com.chibaminto.wearcost.ui.theme.WearCostTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale

private enum class Screen {
    HOME,
    SETTINGS,
    EDIT
}

private data class WearReactionState(
    val operationId: String,
    val change: WearCostChange
)

private data class BatchReactionState(
    val operationId: String,
    val message: String
)

@Composable
fun WearCostApp(viewModel: WearCostViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val updatingWearItemIds by viewModel.updatingWearItemIds.collectAsState()
    var screen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var editingItemId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedCategoryKey by rememberSaveable { mutableStateOf<String?>(null) }
    val editingItem = uiState.items.firstOrNull { it.id == editingItemId }

    BackHandler(enabled = screen != Screen.HOME) {
        editingItemId = null
        screen = Screen.HOME
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            items = uiState.items,
            summary = uiState.summary,
            categoryOptions = uiState.categoryOptions,
            currencyCode = uiState.currencyCode,
            selectedCategoryKey = selectedCategoryKey,
            updatingWearItemIds = updatingWearItemIds,
            onCategorySelected = { selectedCategoryKey = it },
            onSettings = { screen = Screen.SETTINGS },
            onAdd = {
                editingItemId = null
                screen = Screen.EDIT
            },
            onEdit = {
                editingItemId = it.id
                screen = Screen.EDIT
            },
            onRecordWear = viewModel::recordWear,
            onRecordWearBatch = viewModel::recordWearBatch,
            onEditTodayOutfit = viewModel::editTodayOutfit,
            onUndoWear = viewModel::restoreWearSnapshot,
            onUndoWearBatch = viewModel::restoreWearSnapshots,
            onUndoTodayOutfitEdit = viewModel::restoreTodayOutfitEdit
        )

        Screen.SETTINGS -> SettingsScreen(
            currencyCode = uiState.currencyCode,
            onCurrencySelected = viewModel::setCurrencyCode,
            onBack = { screen = Screen.HOME }
        )

        Screen.EDIT -> AddEditItemScreen(
            item = editingItem,
            categoryOptions = uiState.categoryOptions,
            currencyCode = uiState.currencyCode,
            onAddCustomCategory = viewModel::addCustomCategory,
            onDeleteCustomCategory = viewModel::deleteCustomCategory,
            onBack = { screen = Screen.HOME },
            onSave = {
                viewModel.save(it)
                screen = Screen.HOME
            },
            onDelete = {
                viewModel.delete(it)
                screen = Screen.HOME
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    items: List<ClothingEntity>,
    summary: WearCostSummary,
    categoryOptions: List<CategoryOption>,
    currencyCode: String,
    selectedCategoryKey: String?,
    updatingWearItemIds: Set<Long>,
    onCategorySelected: (String?) -> Unit,
    onSettings: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (ClothingEntity) -> Unit,
    onRecordWear: (Long, Long, (WearRecordSnapshot?) -> Unit) -> Unit,
    onRecordWearBatch: (Set<Long>, Long, (BatchWearRecordResult) -> Unit) -> Unit,
    onEditTodayOutfit: (Set<Long>, Set<Long>, Long, (TodayOutfitEditResultState) -> Unit) -> Unit,
    onUndoWear: (WearRecordSnapshot) -> Unit,
    onUndoWearBatch: (List<WearRecordSnapshot>, (List<Long>) -> Unit) -> Unit,
    onUndoTodayOutfitEdit: (TodayOutfitEditResult, (List<Long>) -> Unit) -> Unit
) {
    val view = LocalView.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val wearAddedMessage = stringResource(R.string.wear_added)
    val undoLabel = stringResource(R.string.undo)
    val batchNoRecordMessage = stringResource(R.string.batch_no_new_items)
    val batchMissingItemsMessage = stringResource(R.string.batch_missing_items)
    val batchUndoPartialMessage = stringResource(R.string.batch_undo_partial)
    val batchRecordedFormat = stringResource(R.string.batch_recorded_format)
    val batchReactionFormat = stringResource(R.string.batch_reaction_format)
    val batchReactionMilestoneFormat = stringResource(R.string.batch_reaction_milestone_format)
    val todayOutfitAddedFormat = stringResource(R.string.today_outfit_added_format)
    val todayOutfitRemovedFormat = stringResource(R.string.today_outfit_removed_format)
    val todayOutfitUpdatedMessage = stringResource(R.string.today_outfit_updated)
    var currentLocalEpochDay by remember { mutableStateOf(todayEpochDay()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            currentLocalEpochDay = todayEpochDay()
        }
    }
    val todayEpochDay = currentLocalEpochDay
    var sortOrder by rememberSaveable { mutableStateOf(ClosetSort.RECENTLY_WORN) }
    var duplicateWearItem by remember { mutableStateOf<ClothingEntity?>(null) }
    var confirmedWearItemId by remember { mutableStateOf<Long?>(null) }
    var locallyRecordedDates by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    var pendingWearItemIds by remember { mutableStateOf(setOf<Long>()) }
    var activeUndoState by remember { mutableStateOf<WearUndoState?>(null) }
    var isWearSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedClothingIds by rememberSaveable(stateSaver = clothingIdSetSaver()) {
        mutableStateOf(setOf<Long>())
    }
    var todayRecordedSelectionIds by rememberSaveable(stateSaver = clothingIdSetSaver()) {
        mutableStateOf(setOf<Long>())
    }
    var batchDuplicateIds by remember { mutableStateOf<Set<Long>?>(null) }
    var successFeedbackOperationsById by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var wearReactionsById by remember { mutableStateOf<Map<Long, WearReactionState>>(emptyMap()) }
    var batchReactionState by remember { mutableStateOf<BatchReactionState?>(null) }
    val categoryCounts = remember(items, categoryOptions) { categoryCounts(items, categoryOptions) }
    val filteredItems = remember(items, selectedCategoryKey) {
        selectedCategoryKey?.let { key -> items.filter { itemCategoryKey(it) == key } } ?: items
    }
    val visibleItems = remember(filteredItems, sortOrder) {
        sortClothingItems(filteredItems, sortOrder)
    }
    val editDiff = remember(selectedClothingIds, todayRecordedSelectionIds) {
        todayOutfitEditDiff(
            originalRecordedIds = todayRecordedSelectionIds,
            currentSelectedIds = selectedClothingIds
        )
    }
    val editTargetIds = editDiff.addedIds + editDiff.removedIds
    val isBatchRecording = editTargetIds.any { it in updatingWearItemIds || it in pendingWearItemIds }
    val todayRecordState = remember(items, locallyRecordedDates, todayEpochDay) {
        todayRecordCardState(items, todayEpochDay, locallyRecordedDates)
    }
    val currentTodayRecordedIds = remember(items, locallyRecordedDates, todayEpochDay) {
        todayRecordedClothingIds(items, todayEpochDay, locallyRecordedDates)
    }

    fun startWearSelectionMode() {
        isWearSelectionMode = true
        selectedClothingIds = initialTodayOutfitSelection(currentTodayRecordedIds)
        todayRecordedSelectionIds = currentTodayRecordedIds
    }

    fun clearWearReaction(ids: Set<Long>, operationId: String? = null) {
        successFeedbackOperationsById = successFeedbackOperationsById.filterNot { (id, feedbackOperationId) ->
            id in ids && (operationId == null || feedbackOperationId == operationId)
        }
        wearReactionsById = wearReactionsById.filterNot { (id, reaction) ->
            id in ids && (operationId == null || reaction.operationId == operationId)
        }
    }

    fun scheduleWearReactionClear(
        ids: Set<Long>,
        operationId: String,
        delayMillis: Long = 1_500L
    ) {
        scope.launch {
            kotlinx.coroutines.delay(delayMillis)
            clearWearReaction(ids, operationId)
        }
    }

    LaunchedEffect(items, currentTodayRecordedIds, isWearSelectionMode) {
        val existingIds = items.map { it.id }.toSet()
        val syncedSelection = syncTodayOutfitSelectionIds(
            selectedIds = selectedClothingIds,
            todayRecordedIds = todayRecordedSelectionIds,
            existingIds = existingIds,
            currentTodayRecordedIds = currentTodayRecordedIds,
            isSelectionMode = isWearSelectionMode
        )
        if (syncedSelection.selectedIds != selectedClothingIds) {
            selectedClothingIds = syncedSelection.selectedIds
        }
        if (syncedSelection.todayRecordedIds != todayRecordedSelectionIds) {
            todayRecordedSelectionIds = syncedSelection.todayRecordedIds
        }
        batchDuplicateIds = batchDuplicateIds?.let { it intersect existingIds }
    }

    fun recordWear(
        item: ClothingEntity,
        wornDateEpochDay: Long,
        forceFeedback: Boolean = false
    ) {
        pendingWearItemIds = pendingWearItemIds + item.id
        onRecordWear(item.id, wornDateEpochDay) { snapshot ->
            pendingWearItemIds = pendingWearItemIds - item.id
            if (snapshot == null) return@onRecordWear

            locallyRecordedDates = locallyRecordedDates + (item.id to wornDateEpochDay)
            if (forceFeedback) confirmedWearItemId = item.id
            val change = wearCostChangeForSnapshot(item, snapshot)
            wearReactionsById = wearReactionsById + (
                item.id to WearReactionState(snapshot.operationId, change)
            )
            successFeedbackOperationsById = successFeedbackOperationsById + (item.id to snapshot.operationId)
            scheduleWearReactionClear(setOf(item.id), snapshot.operationId)
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                activeUndoState = WearUndoState.Single(snapshot)
                val result = snackbarHostState.showSnackbar(
                    message = wearAddedMessage,
                    actionLabel = undoLabel,
                    withDismissAction = true
                )
                if (
                    result == SnackbarResult.ActionPerformed &&
                    shouldUndoWear(activeUndoState, snapshot.operationId)
                ) {
                    locallyRecordedDates = locallyRecordedDates - item.id
                    clearWearReaction(setOf(item.id), snapshot.operationId)
                    batchReactionState = null
                    onUndoWear(snapshot)
                }
                if (activeUndoState?.operationId == snapshot.operationId) {
                    activeUndoState = null
                }
            }
        }
    }

    fun recordWearBatch(targetIds: Set<Long>) {
        if (targetIds.isEmpty()) {
            scope.launch { snackbarHostState.showSnackbar(batchNoRecordMessage) }
            return
        }
        val wornDateEpochDay = todayEpochDay()
        pendingWearItemIds = pendingWearItemIds + targetIds
        onRecordWearBatch(targetIds, wornDateEpochDay) { result ->
            pendingWearItemIds = pendingWearItemIds - targetIds
            val snapshots = when (result) {
                BatchWearRecordResult.AlreadyUpdating -> return@onRecordWearBatch
                BatchWearRecordResult.MissingItems -> {
                    scope.launch { snackbarHostState.showSnackbar(batchMissingItemsMessage) }
                    return@onRecordWearBatch
                }
                is BatchWearRecordResult.Success -> result.snapshots
            }
            if (snapshots.isEmpty()) return@onRecordWearBatch

            val recordedIds = snapshots.map { it.clothingId }.toSet()
            val operationId = snapshots.first().operationId
            val changes = wearCostChangesForSnapshots(items, snapshots)
            locallyRecordedDates = locallyRecordedDates + recordedIds.associateWith { wornDateEpochDay }
            successFeedbackOperationsById = successFeedbackOperationsById + recordedIds.associateWith { operationId }
            wearReactionsById = wearReactionsById + changes.mapValues { (_, change) ->
                WearReactionState(operationId, change)
            }
            val milestoneCount = changes.values.count { !it.isFirstWear && it.reachedMilestone != null }
            val batchMessage = if (milestoneCount > 0) {
                batchReactionMilestoneFormat.format(snapshots.size, milestoneCount)
            } else {
                batchReactionFormat.format(snapshots.size)
            }
            batchReactionState = BatchReactionState(operationId, batchMessage)
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            isWearSelectionMode = false
            selectedClothingIds = emptySet()
            scheduleWearReactionClear(recordedIds, operationId)
            scope.launch {
                kotlinx.coroutines.delay(2_200)
                if (batchReactionState?.operationId == operationId) {
                    batchReactionState = null
                }
            }
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val undoState = WearUndoState.Batch(snapshots)
                activeUndoState = undoState
                val result = snackbarHostState.showSnackbar(
                    message = batchRecordedFormat.format(snapshots.size),
                    actionLabel = undoLabel,
                    withDismissAction = true
                )
                if (
                    result == SnackbarResult.ActionPerformed &&
                    shouldUndoWear(activeUndoState, undoState.operationId)
                ) {
                    locallyRecordedDates = locallyRecordedDates - recordedIds
                    val snapshotIds = snapshots.map { it.clothingId }.toSet()
                    wearReactionsById = wearReactionsById.filterNot { (id, reaction) ->
                        id in snapshotIds && reaction.operationId == undoState.operationId
                    }
                    successFeedbackOperationsById = successFeedbackOperationsById.filterNot { (id, feedbackOperationId) ->
                        id in recordedIds && feedbackOperationId == undoState.operationId
                    }
                    if (batchReactionState?.operationId == undoState.operationId) {
                        batchReactionState = null
                    }
                    onUndoWearBatch(snapshots) { missingIds ->
                        if (missingIds.isNotEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar(batchUndoPartialMessage) }
                        }
                    }
                }
                if (activeUndoState?.operationId == undoState.operationId) {
                    activeUndoState = null
                }
            }
        }
    }

    fun handleTodayOutfitEditSubmit() {
        if (!editDiff.hasChanges) return
        val targetIds = editDiff.addedIds + editDiff.removedIds
        pendingWearItemIds = pendingWearItemIds + targetIds
        onEditTodayOutfit(editDiff.addedIds, editDiff.removedIds, todayEpochDay()) { resultState ->
            pendingWearItemIds = pendingWearItemIds - targetIds
            val result = when (resultState) {
                TodayOutfitEditResultState.AlreadyUpdating -> return@onEditTodayOutfit
                TodayOutfitEditResultState.MissingItems -> {
                    scope.launch { snackbarHostState.showSnackbar(batchMissingItemsMessage) }
                    return@onEditTodayOutfit
                }
                is TodayOutfitEditResultState.Success -> resultState.result
            }
            locallyRecordedDates = locallyRecordedDates + result.addedIds.associateWith { todayEpochDay }
            locallyRecordedDates = locallyRecordedDates - result.removedIds
            isWearSelectionMode = false
            selectedClothingIds = emptySet()
            todayRecordedSelectionIds = emptySet()
            val message = when {
                result.addedIds.isNotEmpty() && result.removedIds.isNotEmpty() ->
                    todayOutfitUpdatedMessage
                result.addedIds.isNotEmpty() ->
                    todayOutfitAddedFormat.format(result.addedIds.size)
                else -> todayOutfitRemovedFormat.format(result.removedIds.size)
            }
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                val undoState = WearUndoState.TodayOutfitEdit(result)
                activeUndoState = undoState
                val snackbarResult = snackbarHostState.showSnackbar(
                    message = message,
                    actionLabel = undoLabel,
                    withDismissAction = true
                )
                if (
                    snackbarResult == SnackbarResult.ActionPerformed &&
                    shouldUndoWear(activeUndoState, undoState.operationId)
                ) {
                    locallyRecordedDates = locallyRecordedDates - result.addedIds
                    onUndoTodayOutfitEdit(result) { missingIds ->
                        if (missingIds.isNotEmpty()) {
                            scope.launch { snackbarHostState.showSnackbar(batchUndoPartialMessage) }
                        }
                    }
                }
                if (activeUndoState?.operationId == undoState.operationId) {
                    activeUndoState = null
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    TextButton(onClick = onSettings) {
                        Text(stringResource(R.string.settings))
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isWearSelectionMode) {
                AddClothingFab(onAdd = onAdd)
            }
        },
        bottomBar = {
            if (isWearSelectionMode) {
                BatchRecordBottomBar(
                    addedCount = editDiff.addedIds.size,
                    removedCount = editDiff.removedIds.size,
                    isRecording = isBatchRecording,
                    onRecord = ::handleTodayOutfitEditSubmit
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(WearCostTestTags.HomeList),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 8.dp,
                bottom = if (isWearSelectionMode) 18.dp else 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                if (isWearSelectionMode) {
                    WearSelectionHeader(
                        selectedCount = todayOutfitSelectedCount(selectedClothingIds),
                        isEditingExistingTodayOutfit = todayRecordedSelectionIds.isNotEmpty(),
                        onCancel = {
                            isWearSelectionMode = false
                            selectedClothingIds = emptySet()
                            todayRecordedSelectionIds = emptySet()
                            batchDuplicateIds = null
                        }
                    )
                } else {
                    TodayRecordCard(
                        state = todayRecordState,
                        onStartSelection = ::startWearSelectionMode
                    )
                }
            }

            item {
                SummaryPanel(
                    summary = summary,
                    currencyCode = currencyCode
                )
            }

            item {
                HomeCategoryFilter(
                    options = categoryOptions,
                    selectedKey = selectedCategoryKey,
                    counts = categoryCounts,
                    onSelected = onCategorySelected
                )
            }

            item {
                ClosetSortMenu(
                    selected = sortOrder,
                    onSelected = { sortOrder = it }
                )
            }

            batchReactionState?.let { reaction ->
                item {
                    BatchReactionBanner(message = reaction.message)
                }
            }

            if (visibleItems.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(
                    items = visibleItems,
                    key = { it.id }
                ) { item ->
                    ClothingCard(
                        item = item,
                        currencyCode = currencyCode,
                        todayEpochDay = todayEpochDay,
                        isRecording = item.id in updatingWearItemIds || item.id in pendingWearItemIds,
                        isSelectionMode = isWearSelectionMode,
                        isSelected = item.id in selectedClothingIds,
                        isRecordedTodayInSelection = item.id in todayRecordedSelectionIds,
                        showSuccessFeedback = successFeedbackOperationsById.containsKey(item.id),
                        forceRecordedFeedback = confirmedWearItemId == item.id,
                        wearCostChange = wearReactionsById[item.id]?.change,
                        onEdit = {
                            if (isWearSelectionMode) {
                                selectedClothingIds = if (item.id in selectedClothingIds) {
                                    selectedClothingIds - item.id
                                } else {
                                    selectedClothingIds + item.id
                                }
                            } else {
                                onEdit(item)
                            }
                        },
                        onSelectionChanged = {
                            selectedClothingIds = if (item.id in selectedClothingIds) {
                                selectedClothingIds - item.id
                            } else {
                                selectedClothingIds + item.id
                            }
                        },
                        onFeedbackConsumed = {
                            if (confirmedWearItemId == item.id) confirmedWearItemId = null
                        },
                        onRecordWear = {
                            val wornDateEpochDay = todayEpochDay()
                            if (
                                hasWearRecordedOn(item, wornDateEpochDay) ||
                                locallyRecordedDates[item.id] == wornDateEpochDay
                            ) {
                                duplicateWearItem = item
                                false
                            } else {
                                recordWear(item, wornDateEpochDay)
                                true
                            }
                        }
                    )
                }
            }
        }
    }

    duplicateWearItem?.let { item ->
        AlertDialog(
            onDismissRequest = { duplicateWearItem = null },
            title = { Text(stringResource(R.string.duplicate_wear_title)) },
            text = { Text(stringResource(R.string.duplicate_wear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        duplicateWearItem = null
                        recordWear(item, todayEpochDay(), forceFeedback = true)
                    }
                ) {
                    Text(stringResource(R.string.record_again))
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicateWearItem = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    batchDuplicateIds?.let { duplicateIds ->
        val selectedCount = selectedClothingIds.size
        val duplicateCount = duplicateIds.size
        AlertDialog(
            onDismissRequest = { batchDuplicateIds = null },
            title = { Text(stringResource(R.string.batch_duplicate_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.batch_duplicate_message,
                        selectedCount,
                        duplicateCount
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetIds = selectedClothingIds
                        batchDuplicateIds = null
                        recordWearBatch(targetIds)
                    }
                ) {
                    Text(stringResource(R.string.batch_record_all_again))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            val targetIds = batchTargetIdsExcludingAlreadyRecorded(
                                selectedClothingIds,
                                duplicateIds
                            )
                            batchDuplicateIds = null
                            if (targetIds.isEmpty()) {
                                scope.launch { snackbarHostState.showSnackbar(batchNoRecordMessage) }
                            } else {
                                recordWearBatch(targetIds)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.batch_record_excluding_duplicates))
                    }
                    TextButton(onClick = { batchDuplicateIds = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        )
    }
}

private fun clothingIdSetSaver(): Saver<Set<Long>, LongArray> =
    Saver(
        save = { saveClothingIdSet(it) },
        restore = { restoreClothingIdSet(it) }
    )

@Composable
private fun TodayRecordCard(
    state: TodayRecordCardState,
    onStartSelection: () -> Unit
) {
    if (state is TodayRecordCardState.Recorded) {
        RecordedTodayCard(
            state = state,
            onStartSelection = onStartSelection
        )
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.today_record_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.today_record_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onStartSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag(WearCostTestTags.StartBatchRecord),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.start_today_outfit_record),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun RecordedTodayCard(
    state: TodayRecordCardState.Recorded,
    onStartSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartSelection),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.68f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = state.exactCount?.let {
                        stringResource(R.string.today_recorded_count_format, it)
                    } ?: stringResource(R.string.today_record_exists),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.today_record_add_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(
                onClick = onStartSelection,
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .testTag(WearCostTestTags.StartBatchRecord)
            ) {
                Text(
                    text = stringResource(R.string.edit_today_record),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun BatchReactionBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WearSelectionHeader(
    selectedCount: Int,
    isEditingExistingTodayOutfit: Boolean,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag(WearCostTestTags.SelectionHeader),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEditingExistingTodayOutfit) {
                        stringResource(R.string.selection_mode_edit_title)
                    } else {
                        stringResource(R.string.selection_mode_title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isEditingExistingTodayOutfit) {
                        stringResource(R.string.today_outfit_count_format, selectedCount)
                    } else {
                        stringResource(R.string.selected_count_format, selectedCount)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isEditingExistingTodayOutfit) {
                    Text(
                        text = stringResource(R.string.selection_mode_edit_subtitle),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(
                onClick = onCancel,
                modifier = Modifier.testTag(WearCostTestTags.SelectionCancel)
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
private fun AddClothingFab(onAdd: () -> Unit) {
    val addClothingLabel = stringResource(R.string.add_clothing)
    FloatingActionButton(
        onClick = onAdd,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .testTag(WearCostTestTags.AddClothingFab)
            .semantics {
                contentDescription = addClothingLabel
            }
    ) {
        Text(
            text = stringResource(R.string.add_short),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BatchRecordBottomBar(
    addedCount: Int,
    removedCount: Int,
    isRecording: Boolean,
    onRecord: () -> Unit
) {
    val hasChanges = addedCount > 0 || removedCount > 0
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when {
                    addedCount > 0 && removedCount > 0 -> stringResource(
                        R.string.today_outfit_change_summary_format,
                        addedCount,
                        removedCount
                    )
                    addedCount > 0 -> stringResource(R.string.today_outfit_add_summary_format, addedCount)
                    removedCount > 0 -> stringResource(R.string.today_outfit_remove_summary_format, removedCount)
                    else -> stringResource(R.string.today_outfit_no_changes)
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = onRecord,
                enabled = hasChanges && !isRecording,
                modifier = Modifier
                    .height(52.dp)
                    .testTag(WearCostTestTags.BatchRecordButton),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp)
            ) {
                if (isRecording) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.recording))
                } else {
                    Text(
                        text = stringResource(R.string.save_changes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ClosetSortMenu(
    selected: ClosetSort,
    onSelected: (ClosetSort) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortAccessibility = stringResource(R.string.sort_accessibility)

    Box {
        TextButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(999.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
            modifier = Modifier
                .height(38.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    RoundedCornerShape(999.dp)
                )
                .semantics { contentDescription = sortAccessibility }
        ) {
            Text(
                text = stringResource(R.string.sort_chip_format, closetSortDisplayName(selected)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = (-6).dp)
        ) {
            ClosetSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (sort == selected) "✓" else "",
                                modifier = Modifier.width(18.dp)
                            )
                            Text(closetSortDisplayName(sort))
                        }
                    },
                    onClick = {
                        onSelected(sort)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AppBottomBar(onAdd: () -> Unit) {
    val addClothingLabel = stringResource(R.string.add_clothing)

    BottomActionBar(
        primaryText = addClothingLabel,
        contentDescription = addClothingLabel,
        onPrimaryClick = onAdd
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    currencyCode: String,
    onCurrencySelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = currencyDisplayName(currencyCode),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.currency_no_conversion_note),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text(stringResource(R.string.currency)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SupportedCurrencyCodes.forEach { code ->
                        TextButton(
                            onClick = {
                                onCurrencySelected(code)
                                showCurrencyDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currencyDisplayName(code),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.currency_no_conversion_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun closetSortDisplayName(sort: ClosetSort): String =
    when (sort) {
        ClosetSort.RECENTLY_WORN -> stringResource(R.string.sort_recently_worn)
        ClosetSort.LEAST_RECENTLY_WORN -> stringResource(R.string.sort_least_recently_worn)
        ClosetSort.COST_PER_WEAR_HIGH -> stringResource(R.string.sort_cost_per_wear_high)
        ClosetSort.COST_PER_WEAR_LOW -> stringResource(R.string.sort_cost_per_wear_low)
        ClosetSort.WEAR_COUNT_HIGH -> stringResource(R.string.sort_wear_count_high)
        ClosetSort.WEAR_COUNT_LOW -> stringResource(R.string.sort_wear_count_low)
        ClosetSort.PURCHASE_PRICE_HIGH -> stringResource(R.string.sort_purchase_price_high)
        ClosetSort.PURCHASE_PRICE_LOW -> stringResource(R.string.sort_purchase_price_low)
        ClosetSort.NEWEST_CREATED -> stringResource(R.string.sort_newest_created)
        ClosetSort.OLDEST_CREATED -> stringResource(R.string.sort_oldest_created)
    }

@Composable
private fun HomeCategoryFilter(
    options: List<CategoryOption>,
    selectedKey: String?,
    counts: Map<String?, Int>,
    onSelected: (String?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedKey == null,
                onClick = { onSelected(null) },
                label = {
                    Text(categoryChipLabel(stringResource(R.string.all_categories), counts[null] ?: 0, selectedKey == null))
                },
                shape = RoundedCornerShape(999.dp),
                colors = categoryFilterColors(),
                border = categoryFilterBorder(selectedKey == null)
            )
        }
        items(options, key = { it.key }) { option ->
            val selected = selectedKey == option.key
            FilterChip(
                selected = selected,
                onClick = { onSelected(option.key) },
                label = {
                    Text(
                        text = categoryChipLabel(
                            categoryOptionDisplayName(option),
                            counts[option.key] ?: 0,
                            selected
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = RoundedCornerShape(999.dp),
                colors = categoryFilterColors(),
                border = categoryFilterBorder(selected)
            )
        }
    }
}

private fun categoryChipLabel(label: String, count: Int, selected: Boolean): String =
    if (selected) "✓ $label $count" else "$label $count"

@Composable
private fun categoryFilterColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
)

@Composable
private fun categoryFilterBorder(selected: Boolean) = FilterChipDefaults.filterChipBorder(
    enabled = true,
    selected = selected,
    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.58f),
    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
)

@Composable
private fun SummaryPanel(
    summary: WearCostSummary,
    currencyCode: String
) {
    val heroBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.surface
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .background(heroBrush)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = stringResource(R.string.closet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            ) {
                SummaryGridRow {
                    SummaryValue(
                        label = stringResource(R.string.registered_count),
                        value = stringResource(R.string.closet_item_count_format, summary.itemCount),
                        modifier = Modifier.weight(0.72f)
                    )
                    SummaryGridDivider()
                    SummaryValue(
                        label = stringResource(R.string.purchase_total),
                        value = formatCurrency(summary.totalPurchaseValue, currencyCode),
                        modifier = Modifier.weight(1.38f),
                        addCurrencySpacing = true
                    )
                    SummaryGridDivider()
                    SummaryValue(
                        label = stringResource(R.string.unworn_count),
                        value = stringResource(R.string.closet_item_count_format, summary.unwornCount),
                        supportingValue = summary.unwornPurchaseValue.takeIf { summary.unwornCount > 0 }?.let {
                            stringResource(R.string.unworn_purchase_value_format, formatCurrency(it, currencyCode))
                        },
                        modifier = Modifier.weight(0.90f),
                        subdued = summary.unwornCount == 0
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryGridRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        content = content
    )
}

@Composable
private fun SummaryGridDivider() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    )
}

@Composable
private fun SummaryValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingValue: String? = null,
    addCurrencySpacing: Boolean = false,
    subdued: Boolean = false
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(min = 18.dp)
        )
        Row(
            modifier = Modifier.padding(top = if (addCurrencySpacing) 2.dp else 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (subdued) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            supportingValue?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 22.dp, vertical = 38.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClothingCard(
    item: ClothingEntity,
    currencyCode: String,
    todayEpochDay: Long,
    isRecording: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    isRecordedTodayInSelection: Boolean,
    showSuccessFeedback: Boolean,
    forceRecordedFeedback: Boolean,
    wearCostChange: WearCostChange?,
    onEdit: () -> Unit,
    onSelectionChanged: () -> Unit,
    onFeedbackConsumed: () -> Unit,
    onRecordWear: () -> Boolean
) {
    val view = LocalView.current
    val categoryName = itemCategoryDisplayName(item)
    val unnamedItem = stringResource(R.string.unnamed_item)
    val displayName = clothingDisplayNameOrFallback(
        name = item.name.takeUnless { it.trim() == categoryName }.orEmpty(),
        fallback = unnamedItem
    )
    val isUnnamed = displayName == unnamedItem
    val recordedLastWornEpochDay = lastWornEpochDay(item)
    val isRecordedToday = recordedLastWornEpochDay == todayEpochDay
    val lastWornText = if (recordedLastWornEpochDay == null && item.wearCount > 0) {
        stringResource(R.string.last_worn_unknown)
    } else {
        lastWornDisplayText(relativeLastWornText(recordedLastWornEpochDay, todayEpochDay))
    }
    val currentCostPerWear = costPerWear(item.purchasePrice, item.wearCount)
    val accessibilityCostPerWear = currentCostPerWear?.let { formatCurrency(it, currencyCode) }
        ?: stringResource(R.string.not_worn_yet_subtitle)
    val todayWoreAccessibility = stringResource(
        R.string.today_wore_accessibility,
        displayName,
        accessibilityCostPerWear,
        item.wearCount
    )
    val animationsEnabled = remember {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || ValueAnimator.areAnimatorsEnabled()
    }
    val animatedCostPerWear by animateIntAsState(
        targetValue = currentCostPerWear ?: 0,
        animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0),
        label = "costPerWear"
    )
    var recordedFeedback by remember(item.id) { mutableStateOf(false) }
    var selectionPulse by remember(item.id) { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (recordedFeedback && animationsEnabled) 0.96f else 1f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 150 else 0),
        label = "wearButtonScale"
    )
    val cardScale by animateFloatAsState(
        targetValue = if ((selectionPulse || showSuccessFeedback) && animationsEnabled) 0.985f else 1f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 140 else 0),
        label = "selectionCardScale"
    )
    val costPanelScale by animateFloatAsState(
        targetValue = if (wearCostChange != null && animationsEnabled) 1.025f else 1f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0),
        label = "costPanelScale"
    )
    val isKeptRecorded = isSelectionMode && isRecordedTodayInSelection && isSelected
    val isAddedInEdit = isSelectionMode && !isRecordedTodayInSelection && isSelected
    val isRemovalPending = isSelectionMode && isRecordedTodayInSelection && !isSelected
    val hasSelectionCheck = isKeptRecorded || isAddedInEdit || showSuccessFeedback
    val selectionStatusText = when {
        isKeptRecorded -> stringResource(R.string.already_recorded_label)
        isAddedInEdit -> stringResource(R.string.add_pending_label)
        isRemovalPending -> stringResource(R.string.remove_pending_label)
        else -> null
    }

    LaunchedEffect(recordedFeedback) {
        if (recordedFeedback) {
            kotlinx.coroutines.delay(220)
            recordedFeedback = false
            onFeedbackConsumed()
        }
    }

    LaunchedEffect(forceRecordedFeedback) {
        if (forceRecordedFeedback) {
            recordedFeedback = true
        }
    }

    LaunchedEffect(selectionPulse) {
        if (selectionPulse) {
            kotlinx.coroutines.delay(150)
            selectionPulse = false
        }
    }

    val cardShape = RoundedCornerShape(24.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(WearCostTestTags.clothingCard(item.id))
            .then(
                if (cardScale != 1f) Modifier.graphicsLayer {
                    scaleX = cardScale
                    scaleY = cardScale
                } else Modifier
            )
            .clickable {
                if (isSelectionMode) {
                    selectionPulse = true
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onSelectionChanged()
                } else {
                    onEdit()
                }
            }
            .semantics {
                if (isSelectionMode) {
                    selected = isSelected
                }
            },
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isKeptRecorded || isAddedInEdit) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f)
            } else if (isRemovalPending) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = if (isKeptRecorded || isAddedInEdit || isRemovalPending) 2.dp else 1.dp,
            color = if (isAddedInEdit) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.58f)
            } else if (isKeptRecorded) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
            } else if (isRemovalPending) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.44f)
            } else if (showSuccessFeedback) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.36f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.46f)
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box {
                ItemImage(
                    imageUri = item.imageUri,
                    category = item.category,
                    modifier = Modifier.size(108.dp)
                )
                if (hasSelectionCheck) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayName,
                    style = if (isUnnamed) {
                        MaterialTheme.typography.bodyLarge
                    } else {
                        MaterialTheme.typography.titleMedium
                    },
                    fontWeight = if (isUnnamed) {
                        FontWeight.Normal
                    } else {
                        FontWeight.Bold
                    },
                    color = if (isUnnamed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    selectionStatusText?.let {
                        ItemMetaChip(
                            text = it,
                            emphasized = isAddedInEdit || isKeptRecorded,
                            warning = isRemovalPending
                        )
                    }
                    Text(
                        text = stringResource(
                            if (recordedLastWornEpochDay == null) {
                                R.string.category_and_status_format
                            } else {
                                R.string.category_and_last_worn_format
                            },
                            categoryName,
                            lastWornText
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CostPerWearPanel(
                    wearCount = item.wearCount,
                    animatedCostPerWear = animatedCostPerWear,
                    currencyCode = currencyCode,
                    wearCostChange = wearCostChange,
                    modifier = if (costPanelScale != 1f) {
                        Modifier.graphicsLayer {
                            scaleX = costPanelScale
                            scaleY = costPanelScale
                        }
                    } else Modifier
                )
                Text(
                    text = stringResource(
                        R.string.wear_count_and_purchase_format,
                        item.wearCount,
                        formatCurrency(item.purchasePrice, currencyCode)
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isSelectionMode) {
                    Button(
                        onClick = {
                            if (onRecordWear()) {
                                recordedFeedback = true
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                        },
                        enabled = !isRecording,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag(WearCostTestTags.todayWoreButton(item.id))
                            .then(
                                if (buttonScale != 1f) Modifier.graphicsLayer {
                                    scaleX = buttonScale
                                    scaleY = buttonScale
                                } else Modifier
                            )
                            .semantics {
                                contentDescription = todayWoreAccessibility
                            },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecordedToday) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            contentColor = if (isRecordedToday) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = if (recordedFeedback || isRecordedToday) {
                                stringResource(R.string.recorded_with_check)
                            } else {
                                stringResource(R.string.today_wore)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CostPerWearPanel(
    wearCount: Int,
    animatedCostPerWear: Int,
    currencyCode: String,
    wearCostChange: WearCostChange?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.58f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(R.string.cost_per_wear_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (wearCount > 0) {
                formatCurrency(animatedCostPerWear, currencyCode)
            } else {
                stringResource(R.string.cost_per_wear_unknown)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (wearCount == 0) {
            Text(
                text = stringResource(R.string.cost_calculated_after_wear),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        wearCostChange?.let { change ->
            WearCostReactionMessage(
                change = change,
                currencyCode = currencyCode
            )
        }
    }
}

@Composable
private fun WearCostReactionMessage(
    change: WearCostChange,
    currencyCode: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .testTag(WearCostTestTags.wearCostReaction(change.clothingId)),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        val milestone = change.reachedMilestone.takeUnless { change.isFirstWear }
        val mainText = when {
            change.isFirstWear -> stringResource(R.string.first_wear_recorded)
            milestone != null -> milestoneMessage(milestone)
            change.savedPerWear != null -> stringResource(
                R.string.cost_down_format,
                formatCurrency(change.savedPerWear, currencyCode)
            )
            else -> stringResource(R.string.cost_improved)
        }
        val detailText = when {
            change.isFirstWear -> stringResource(
                R.string.first_wear_start_format,
                formatCurrency(change.newCostPerWear ?: 0, currencyCode)
            )
            change.previousCostPerWear != null && change.newCostPerWear != null -> stringResource(
                R.string.cost_transition_format,
                formatCurrency(change.previousCostPerWear, currencyCode),
                formatCurrency(change.newCostPerWear, currencyCode)
            )
            change.newCostPerWear != null -> stringResource(
                R.string.cost_per_wear_format,
                formatCurrency(change.newCostPerWear, currencyCode)
            )
            else -> null
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (milestone != null) {
                Text(
                    text = "✦",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = mainText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        detailText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun milestoneMessage(milestone: Int): String =
    stringResource(R.string.wear_milestone_format, milestone)

@Composable
private fun ItemMetaChip(
    text: String,
    emphasized: Boolean = false,
    warning: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = when {
            warning -> MaterialTheme.colorScheme.onTertiaryContainer
            emphasized -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(
                when {
                    warning -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.46f)
                    emphasized -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
                }
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun ItemImage(
    imageUri: String?,
    category: ClothingCategory,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    if (imageUri.isNullOrBlank()) {
        CategoryPlaceholder(category = category, modifier = modifier)
        return
    }

    var hasError by remember(imageUri) { mutableStateOf(false) }
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f), shape)
    ) {
        if (hasError) {
            CategoryPlaceholder(
                category = category,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.item_image),
                contentScale = ContentScale.Crop,
                onError = { hasError = true },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun CategoryPlaceholder(
    category: ClothingCategory,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = categoryShortName(category),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditItemScreen(
    item: ClothingEntity?,
    categoryOptions: List<CategoryOption>,
    currencyCode: String,
    onAddCustomCategory: (String, (CustomCategoryEntity?) -> Unit) -> Unit,
    onDeleteCustomCategory: (CustomCategoryEntity) -> Unit,
    onBack: () -> Unit,
    onSave: (ClothingEntity) -> Unit,
    onDelete: (ClothingEntity) -> Unit
) {
    val context = LocalContext.current
    var name by rememberSaveable(item?.id) { mutableStateOf(item?.name.orEmpty()) }
    var selectedCategoryKey by rememberSaveable(item?.id) {
        mutableStateOf(
            item?.let {
                it.customCategoryName
                    .takeIf { name -> !name.isNullOrBlank() }
                    ?.let { name -> customCategoryKey(name) }
                    ?: fixedCategoryKey(it.category)
            }
        )
    }
    var priceText by rememberSaveable(item?.id) {
        mutableStateOf(item?.purchasePrice?.takeIf { it > 0 }?.toString().orEmpty())
    }
    var wearCountText by rememberSaveable(item?.id) { mutableStateOf((item?.wearCount ?: 0).toString()) }
    var purchaseDateMillis by rememberSaveable(item?.id) { mutableStateOf(item?.purchaseDateMillis) }
    var imageUri by rememberSaveable(item?.id) { mutableStateOf(item?.imageUri) }
    var pendingCameraUri by rememberSaveable(item?.id) { mutableStateOf<String?>(null) }
    var showOptionalDetails by rememberSaveable(item?.id) {
        mutableStateOf(
            item != null &&
                (item.name.isNotBlank() || item.wearCount > 0 || item.purchaseDateMillis != null)
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var saveAttempted by rememberSaveable(item?.id) { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            ImageStorage.copyFrom(context, uri)?.let { copiedUri ->
                imageUri = copiedUri.toString()
            }
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = pendingCameraUri
        }
    }

    val price = priceText.toIntOrNull() ?: 0
    val wearCount = wearCountText.toIntOrNull() ?: 0
    var pendingCustomCategory by remember(item?.id) {
        mutableStateOf<CustomCategoryEntity?>(null)
    }
    val editCategoryOptions = remember(item?.id, item?.category, item?.customCategoryName, categoryOptions, pendingCustomCategory) {
        addEditCategoryOptions(item, categoryOptions, pendingCustomCategory)
    }
    val selectedCategoryOption = editCategoryOptions.firstOrNull { it.key == selectedCategoryKey }
    val selectedCustomCategoryName = selectedCategoryOption?.customCategory?.name
        ?: selectedCategoryKey?.removePrefix("custom:")?.takeIf {
            selectedCategoryKey?.startsWith("custom:") == true && it.isNotBlank()
        }
    val selectedFixedCategory = selectedCategoryOption?.fixedCategory
    val previewCategory = selectedFixedCategory ?: ClothingCategory.OTHER
    val fallbackName = selectedCustomCategoryName
        ?: selectedCategoryOption?.let { categoryOptionDisplayName(it) }
        ?: stringResource(R.string.name)
    val hasImage = !imageUri.isNullOrBlank()
    val hasPrice = priceText.isNotBlank() && price > 0
    val hasCategory = !selectedCategoryKey.isNullOrBlank()
    val canSave = canSaveClothingDraft(hasImage, hasPrice, selectedCategoryKey, wearCount)
    val saveLabel = if (item == null) stringResource(R.string.add_item) else stringResource(R.string.save)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (item == null) stringResource(R.string.add_item)
                        else stringResource(R.string.edit_item)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            SurfaceBottomAction(
                primaryText = saveLabel,
                enabled = true,
                onPrimaryClick = {
                    saveAttempted = true
                    if (!canSave || selectedCategoryOption == null) return@SurfaceBottomAction
                    val categoryToSave = selectedFixedCategory ?: ClothingCategory.OTHER
                    onSave(
                        ClothingEntity(
                            id = item?.id ?: 0L,
                            name = name.trim().ifEmpty { fallbackName },
                            category = categoryToSave,
                            purchasePrice = price,
                            purchaseDateMillis = purchaseDateMillis,
                            wearCount = wearCount,
                            imageUri = imageUri,
                            customCategoryName = selectedCustomCategoryName,
                            lastWornDateEpochDay = item?.lastWornDateEpochDay,
                            lastWearRecordedAtMillis = item?.lastWearRecordedAtMillis,
                            createdAtMillis = item?.createdAtMillis ?: System.currentTimeMillis()
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                QuickAddCard(
                    imageUri = imageUri,
                    category = previewCategory,
                    categoryOptions = editCategoryOptions,
                    selectedCategoryKey = selectedCategoryKey,
                    priceText = priceText,
                    currencyCode = currencyCode,
                    showValidation = saveAttempted,
                    hasImage = hasImage,
                    hasPrice = hasPrice,
                    hasCategory = hasCategory,
                    onTakePhoto = {
                        val uri = ImageStorage.createCameraUri(context)
                        pendingCameraUri = uri.toString()
                        cameraLauncher.launch(uri)
                    },
                    onChooseImage = {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onRemoveImage = { imageUri = null },
                    onCategorySelected = { selectedCategoryKey = it },
                    onAddCustomCategory = { customCategoryName, onResult ->
                        val normalizedName = customCategoryName.trim()
                        if (normalizedName.isNotEmpty()) {
                            onAddCustomCategory(normalizedName) { createdCategory ->
                                pendingCustomCategory = createdCategory
                                createdCategory?.let {
                                    selectedCategoryKey = customCategoryKey(it.name)
                                }
                                onResult(createdCategory)
                            }
                        }
                    },
                    onDeleteCustomCategory = {
                        onDeleteCustomCategory(it)
                        if (selectedCategoryKey == customCategoryKey(it.name)) {
                            selectedCategoryKey = null
                        }
                    },
                    onPriceChanged = { priceText = it.filter(Char::isDigit) }
                )
            }

            item {
                if (showOptionalDetails) {
                    OptionalDetailsCard(
                        name = name,
                        fallbackName = fallbackName,
                        wearCountText = wearCountText,
                        purchaseDateMillis = purchaseDateMillis,
                        onNameChanged = { name = it },
                        onWearCountChanged = { wearCountText = it.filter(Char::isDigit) },
                        onPurchaseDateSelected = { purchaseDateMillis = it }
                    )
                } else {
                    OptionalDetailsPrompt(onClick = { showOptionalDetails = true })
                }
            }

            if (item != null) {
                item {
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }

    if (showDeleteDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_item_title)) },
            text = { Text(stringResource(R.string.delete_item_message)) },
            confirmButton = {
                TextButton(onClick = { onDelete(item) }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun OptionalDetailsPrompt(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.show_optional_details),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.optional_details_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickAddCard(
    imageUri: String?,
    category: ClothingCategory,
    categoryOptions: List<CategoryOption>,
    selectedCategoryKey: String?,
    priceText: String,
    currencyCode: String,
    showValidation: Boolean,
    hasImage: Boolean,
    hasPrice: Boolean,
    hasCategory: Boolean,
    onTakePhoto: () -> Unit,
    onChooseImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onCategorySelected: (String) -> Unit,
    onAddCustomCategory: (String, (CustomCategoryEntity?) -> Unit) -> Unit,
    onDeleteCustomCategory: (CustomCategoryEntity) -> Unit,
    onPriceChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ItemImage(
                    imageUri = imageUri,
                    category = category,
                    modifier = Modifier.size(136.dp)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onTakePhoto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(stringResource(R.string.take_photo))
                    }
                    OutlinedButton(
                        onClick = onChooseImage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(stringResource(R.string.choose_image))
                    }
                    if (!imageUri.isNullOrBlank()) {
                        TextButton(
                            onClick = onRemoveImage,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.remove_image))
                        }
                    }
                    if (shouldShowRequiredFieldError(showValidation, hasImage)) {
                        RequiredFieldText(text = stringResource(R.string.required_image))
                    }
                }
            }

            CategorySelector(
                options = categoryOptions,
                selectedKey = selectedCategoryKey,
                onSelected = onCategorySelected,
                onAddCustomCategory = onAddCustomCategory,
                onDeleteCustomCategory = onDeleteCustomCategory
            )
            if (shouldShowRequiredFieldError(showValidation, hasCategory)) {
                RequiredFieldText(text = stringResource(R.string.required_category))
            }

            OutlinedTextField(
                value = priceText,
                onValueChange = onPriceChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.purchase_price)) },
                placeholder = { Text(stringResource(R.string.price_placeholder)) },
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = shouldShowRequiredFieldError(showValidation, hasPrice),
                supportingText = {
                    if (shouldShowRequiredFieldError(showValidation, hasPrice)) {
                        Text(stringResource(R.string.required_price))
                    } else {
                        Text(stringResource(R.string.price_unit_format, currencyDisplayName(currencyCode)))
                    }
                }
            )
        }
    }
}

@Composable
private fun RequiredFieldText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error
    )
}

@Composable
private fun OptionalDetailsCard(
    name: String,
    fallbackName: String,
    wearCountText: String,
    purchaseDateMillis: Long?,
    onNameChanged: (String) -> Unit,
    onWearCountChanged: (String) -> Unit,
    onPurchaseDateSelected: (Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.optional_details),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.name_optional)) },
                placeholder = { Text(fallbackName) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = wearCountText,
                    onValueChange = onWearCountChanged,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.initial_wear_count)) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                PurchaseDatePickerField(
                    purchaseDateMillis = purchaseDateMillis,
                    onPurchaseDateSelected = onPurchaseDateSelected,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PurchaseDatePickerField(
    purchaseDateMillis: Long?,
    onPurchaseDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val displayDate = purchaseDateMillis?.let { formatDate(it) }

    OutlinedButton(
        onClick = { showDatePicker = true },
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.purchase_date),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = displayDate ?: stringResource(R.string.select_date),
                style = MaterialTheme.typography.bodyMedium,
                color = if (displayDate == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = purchaseDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onPurchaseDateSelected(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.select))
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            onPurchaseDateSelected(null)
                            showDatePicker = false
                        }
                    ) {
                        Text(stringResource(R.string.clear))
                    }
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun SurfaceBottomAction(
    primaryText: String,
    enabled: Boolean,
    onPrimaryClick: () -> Unit
) {
    BottomActionBar(
        primaryText = primaryText,
        enabled = enabled,
        imeAware = true,
        onPrimaryClick = onPrimaryClick
    )
}

@Composable
private fun BottomActionBar(
    primaryText: String,
    enabled: Boolean = true,
    contentDescription: String? = null,
    imeAware: Boolean = false,
    onPrimaryClick: () -> Unit
) {
    val barModifier = if (imeAware) {
        Modifier
            .fillMaxWidth()
            .imePadding()
    } else {
        Modifier.fillMaxWidth()
    }

    NavigationBar(
        modifier = barModifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp
    ) {
        Button(
            onClick = onPrimaryClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .height(54.dp)
                .then(
                    contentDescription?.let {
                        Modifier.semantics { this.contentDescription = it }
                    } ?: Modifier
                ),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategorySelector(
    options: List<CategoryOption>,
    selectedKey: String?,
    onSelected: (String) -> Unit,
    onAddCustomCategory: (String, (CustomCategoryEntity?) -> Unit) -> Unit,
    onDeleteCustomCategory: (CustomCategoryEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryPendingDelete by remember { mutableStateOf<CustomCategoryEntity?>(null) }
    val optionLabels = options.associate { it.key to categoryOptionDisplayName(it) }
    val selectedCustomCategory = options.firstOrNull { it.key == selectedKey }?.customCategory

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(options, key = { it.key }) { option ->
                FilterChip(
                    selected = selectedKey == option.key,
                    onClick = { onSelected(option.key) },
                    leadingIcon = if (selectedKey == option.key) {
                        {
                            Text(
                                text = "✓",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        null
                    },
                    label = {
                        Text(
                            text = categoryOptionDisplayName(option),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    shape = RoundedCornerShape(999.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedKey == option.key,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            item {
                FilterChip(
                    selected = false,
                    onClick = { showAddDialog = true },
                    label = { Text(stringResource(R.string.add_category_chip)) },
                    shape = RoundedCornerShape(999.dp),
                    colors = categoryFilterColors()
                )
            }
        }
        if (selectedCustomCategory != null) {
            TextButton(
                onClick = { categoryPendingDelete = selectedCustomCategory },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.delete_selected_category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            existingNames = optionLabels,
            onDismiss = { showAddDialog = false },
            onSelectExisting = {
                onSelected(it)
                showAddDialog = false
            },
            onAdd = { name, onComplete ->
                onAddCustomCategory(name) { createdCategory ->
                    if (createdCategory != null) {
                        onSelected(customCategoryKey(createdCategory.name))
                        showAddDialog = false
                    }
                    onComplete(createdCategory != null)
                }
            }
        )
    }

    categoryPendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryPendingDelete = null },
            title = { Text(stringResource(R.string.delete_category_title)) },
            text = { Text(stringResource(R.string.delete_category_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCustomCategory(category)
                        onSelected(fixedCategoryKey(ClothingCategory.OTHER))
                        categoryPendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(
    existingNames: Map<String, String>,
    onDismiss: () -> Unit,
    onSelectExisting: (String) -> Unit,
    onAdd: (String, (Boolean) -> Unit) -> Unit
) {
    var categoryName by rememberSaveable { mutableStateOf("") }
    var isAdding by rememberSaveable { mutableStateOf(false) }
    var showAddError by rememberSaveable { mutableStateOf(false) }
    val normalizedName = categoryName.trim()
    val existingKey = existingNames.entries.firstOrNull {
        it.value.equals(normalizedName, ignoreCase = true)
    }?.key
    val canSubmit = normalizedName.isNotEmpty() && !isAdding

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_category_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        showAddError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.category_name)) },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    isError = showAddError
                )
                if (showAddError) {
                    RequiredFieldText(text = stringResource(R.string.add_category_failed))
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    if (existingKey != null) onSelectExisting(existingKey)
                    else {
                        isAdding = true
                        onAdd(normalizedName) { success ->
                            isAdding = false
                            showAddError = !success
                        }
                    }
                }
            ) {
                Text(
                    stringResource(
                        when {
                            isAdding -> R.string.recording
                            existingKey != null -> R.string.select
                            else -> R.string.add
                        }
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun categoryDisplayName(category: ClothingCategory): String =
    when (category) {
        ClothingCategory.TOP -> stringResource(R.string.category_top)
        ClothingCategory.BOTTOM -> stringResource(R.string.category_bottom)
        ClothingCategory.OUTER -> stringResource(R.string.category_outer)
        ClothingCategory.SHOES -> stringResource(R.string.category_shoes)
        ClothingCategory.BAG -> stringResource(R.string.category_bag)
        ClothingCategory.ACCESSORY -> stringResource(R.string.category_accessory)
        ClothingCategory.OTHER -> stringResource(R.string.category_other)
    }

@Composable
private fun categoryShortName(category: ClothingCategory): String =
    when (category) {
        ClothingCategory.TOP -> stringResource(R.string.category_top_short)
        ClothingCategory.BOTTOM -> stringResource(R.string.category_bottom_short)
        ClothingCategory.OUTER -> stringResource(R.string.category_outer_short)
        ClothingCategory.SHOES -> stringResource(R.string.category_shoes_short)
        ClothingCategory.BAG -> stringResource(R.string.category_bag_short)
        ClothingCategory.ACCESSORY -> stringResource(R.string.category_accessory_short)
        ClothingCategory.OTHER -> stringResource(R.string.category_other_short)
    }

@Composable
private fun categoryOptionDisplayName(option: CategoryOption): String =
    option.customCategory?.name ?: option.fixedCategory?.let { categoryDisplayName(it) }.orEmpty()

private fun addEditCategoryOptions(
    item: ClothingEntity?,
    categoryOptions: List<CategoryOption>,
    pendingCustomCategory: CustomCategoryEntity? = null
): List<CategoryOption> {
    val primaryKeys = AddEditPrimaryCategories.map(::fixedCategoryKey).toSet()
    val primaryOptions = categoryOptions.filter { it.key in primaryKeys }
    val customOptions = categoryOptions.filter { it.customCategory != null }
    val selectedKey = item?.customCategoryName
        ?.takeIf { it.isNotBlank() }
        ?.let(::customCategoryKey)
        ?: item?.category?.let(::fixedCategoryKey)
    val selectedOption = selectedKey?.let { key -> categoryOptions.firstOrNull { it.key == key } }
    val pendingOption = pendingCustomCategory?.let { CategoryOption(customCategory = it) }
    return (primaryOptions + customOptions + listOfNotNull(selectedOption, pendingOption)).distinctBy { it.key }
}

@Composable
private fun itemCategoryDisplayName(item: ClothingEntity): String =
    item.customCategoryName?.takeIf { it.isNotBlank() } ?: categoryDisplayName(item.category)

@Composable
private fun lastWornDisplayText(distance: LastWornDistance): String =
    when (distance) {
        LastWornDistance.NotWorn -> stringResource(R.string.not_worn_yet_subtitle)
        LastWornDistance.Today -> stringResource(R.string.last_worn_today)
        LastWornDistance.Yesterday -> stringResource(R.string.last_worn_yesterday)
        is LastWornDistance.DaysAgo -> stringResource(R.string.last_worn_days_ago, distance.days)
        is LastWornDistance.WeeksAgo -> stringResource(R.string.last_worn_weeks_ago, distance.weeks)
    }

@Composable
private fun formatCurrency(value: Int, currencyCode: String): String =
    formatCurrency(value.toDouble(), currencyCode)

@Composable
private fun formatCurrency(value: Double, currencyCode: String): String =
    remember(value, currencyCode) {
        formatCurrencyValue(value, currencyCode)
    }

private fun formatCurrencyValue(value: Double, currencyCode: String): String {
    val currency = Currency.getInstance(currencyCode)
    val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        this.currency = currency
    }
    val zeroDecimalCurrency = currencyCode == "JPY" || currencyCode == "KRW"

    if (value > 0.0) {
        if (zeroDecimalCurrency && value < 1.0) {
            return "${currency.getSymbol(Locale.US)}1未満"
        }
        if (!zeroDecimalCurrency && value < 0.01) {
            formatter.minimumFractionDigits = 2
            formatter.maximumFractionDigits = 2
            return "<${formatter.format(0.01)}"
        }
    }

    formatter.minimumFractionDigits = if (zeroDecimalCurrency) 0 else 2
    formatter.maximumFractionDigits = if (zeroDecimalCurrency) 0 else 2
    return formatter.format(value)
}

@Composable
private fun currencyDisplayName(currencyCode: String): String =
    when (currencyCode) {
        "JPY" -> stringResource(R.string.currency_jpy)
        "USD" -> stringResource(R.string.currency_usd)
        "EUR" -> stringResource(R.string.currency_eur)
        "GBP" -> stringResource(R.string.currency_gbp)
        "KRW" -> stringResource(R.string.currency_krw)
        "CNY" -> stringResource(R.string.currency_cny)
        else -> currencyCode
    }

private fun dateFormatter(): SimpleDateFormat =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

private fun formatDate(value: Long): String =
    dateFormatter().format(Date(value))

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun HomeScreenPreview() {
    HomeScreenPreviewContent(darkTheme = false)
}

@Preview(showBackground = true, widthDp = 390, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    HomeScreenPreviewContent(darkTheme = true)
}

@Composable
private fun HomeScreenPreviewContent(darkTheme: Boolean) {
    val items = listOf(
        ClothingEntity(
            id = 1,
            name = "黒Tシャツ",
            category = ClothingCategory.TOP,
            purchasePrice = 4000,
            wearCount = 120
        ),
        ClothingEntity(
            id = 2,
            name = "ワイドパンツ",
            category = ClothingCategory.BOTTOM,
            purchasePrice = 7500,
            wearCount = 18
        ),
        ClothingEntity(
            id = 3,
            name = "白スニーカー",
            category = ClothingCategory.SHOES,
            purchasePrice = 9800,
            wearCount = 0
        )
    )
    WearCostTheme(darkTheme = darkTheme) {
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
            onRecordWear = { _, _, onRecorded -> onRecorded(null) },
            onRecordWearBatch = { _, _, onRecorded -> onRecorded(BatchWearRecordResult.Success(emptyList())) },
            onEditTodayOutfit = { _, _, _, onEdited ->
                onEdited(
                    TodayOutfitEditResultState.Success(
                        TodayOutfitEditResult(
                            operationId = "preview",
                            snapshots = emptyList(),
                            removedRecords = emptyList(),
                            addedIds = emptySet(),
                            removedIds = emptySet()
                        )
                    )
                )
            },
            onUndoWear = {},
            onUndoWearBatch = { _, onRestored -> onRestored(emptyList()) },
            onUndoTodayOutfitEdit = { _, onRestored -> onRestored(emptyList()) }
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun SettingsScreenPreview() {
    WearCostTheme {
        SettingsScreen(
            currencyCode = "JPY",
            onCurrencySelected = {},
            onBack = {}
        )
    }
}
