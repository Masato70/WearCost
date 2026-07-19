package com.chibaminto.wearcost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chibaminto.wearcost.data.ClothingCategory
import com.chibaminto.wearcost.data.ClothingEntity
import com.chibaminto.wearcost.data.ClothingRepository
import com.chibaminto.wearcost.data.CurrencySettingsRepository
import com.chibaminto.wearcost.data.CustomCategoryEntity
import com.chibaminto.wearcost.data.BatchWearRecordResult
import com.chibaminto.wearcost.data.TodayOutfitEditResult
import com.chibaminto.wearcost.data.TodayOutfitEditResultState
import com.chibaminto.wearcost.data.WearRecordSnapshot
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryOption(
    val fixedCategory: ClothingCategory? = null,
    val customCategory: CustomCategoryEntity? = null
) {
    val key: String =
        fixedCategory?.let { fixedCategoryKey(it) } ?: customCategory?.let { customCategoryKey(it.name) }.orEmpty()
}

data class WearCostUiState(
    val items: List<ClothingEntity> = emptyList(),
    val summary: WearCostSummary = WearCostSummary(),
    val categoryOptions: List<CategoryOption> = fixedCategoryOptions(),
    val currencyCode: String = "USD"
)

class WearCostViewModel(
    private val repository: ClothingRepository,
    private val currencySettingsRepository: CurrencySettingsRepository
) : ViewModel() {
    private val wearUpdateGuard = WearUpdateGuard()
    val updatingWearItemIds: StateFlow<Set<Long>> = wearUpdateGuard.updatingItemIds

    val uiState: StateFlow<WearCostUiState> = combine(
        repository.items,
        repository.customCategories,
        currencySettingsRepository.currencyCode
    ) { items, customCategories, currencyCode ->
            val options = fixedCategoryOptions() + customCategories.map {
                CategoryOption(customCategory = it)
            }
            WearCostUiState(
                items = items,
                summary = closetSummary(items),
                categoryOptions = options,
                currencyCode = currencyCode
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WearCostUiState()
        )

    fun save(item: ClothingEntity) {
        viewModelScope.launch {
            repository.save(item)
        }
    }

    fun delete(item: ClothingEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun recordWear(
        itemId: Long,
        wornDateEpochDay: Long,
        onRecorded: (WearRecordSnapshot?) -> Unit
    ) {
        if (!wearUpdateGuard.tryStart(itemId)) {
            onRecorded(null)
            return
        }
        viewModelScope.launch {
            try {
                onRecorded(repository.recordWear(itemId, wornDateEpochDay))
            } finally {
                wearUpdateGuard.finish(itemId)
            }
        }
    }

    fun restoreWearSnapshot(snapshot: WearRecordSnapshot) {
        viewModelScope.launch {
            repository.restoreWearSnapshot(snapshot)
        }
    }

    fun recordWearBatch(
        itemIds: Set<Long>,
        wornDateEpochDay: Long,
        onRecorded: (BatchWearRecordResult) -> Unit
    ) {
        if (!wearUpdateGuard.tryStartAll(itemIds)) {
            onRecorded(BatchWearRecordResult.AlreadyUpdating)
            return
        }
        viewModelScope.launch {
            try {
                onRecorded(repository.recordWearBatch(itemIds, wornDateEpochDay))
            } finally {
                wearUpdateGuard.finishAll(itemIds)
            }
        }
    }

    fun restoreWearSnapshots(
        snapshots: List<WearRecordSnapshot>,
        onRestored: (List<Long>) -> Unit
    ) {
        viewModelScope.launch {
            onRestored(repository.restoreWearSnapshots(snapshots))
        }
    }

    fun editTodayOutfit(
        addedIds: Set<Long>,
        removedIds: Set<Long>,
        wornDateEpochDay: Long,
        onEdited: (TodayOutfitEditResultState) -> Unit
    ) {
        val targetIds = addedIds + removedIds
        if (!wearUpdateGuard.tryStartAll(targetIds)) {
            onEdited(TodayOutfitEditResultState.AlreadyUpdating)
            return
        }
        viewModelScope.launch {
            try {
                onEdited(repository.editTodayOutfit(addedIds, removedIds, wornDateEpochDay))
            } finally {
                wearUpdateGuard.finishAll(targetIds)
            }
        }
    }

    fun restoreTodayOutfitEdit(
        result: TodayOutfitEditResult,
        onRestored: (List<Long>) -> Unit
    ) {
        viewModelScope.launch {
            onRestored(repository.restoreTodayOutfitEdit(result))
        }
    }

    fun addCustomCategory(name: String, onResult: (CustomCategoryEntity?) -> Unit = {}) {
        viewModelScope.launch {
            onResult(repository.addCustomCategory(name))
        }
    }

    fun deleteCustomCategory(category: CustomCategoryEntity) {
        viewModelScope.launch {
            repository.deleteCustomCategory(category)
        }
    }

    fun setCurrencyCode(code: String) {
        currencySettingsRepository.setCurrencyCode(code)
    }
}

class WearCostViewModelFactory(
    private val repository: ClothingRepository,
    private val currencySettingsRepository: CurrencySettingsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WearCostViewModel::class.java)) {
            return WearCostViewModel(repository, currencySettingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

val FixedClothingCategories: List<ClothingCategory> = listOf(
    ClothingCategory.TOP,
    ClothingCategory.BOTTOM,
    ClothingCategory.OUTER,
    ClothingCategory.SHOES,
    ClothingCategory.BAG,
    ClothingCategory.ACCESSORY,
    ClothingCategory.OTHER
)

fun fixedCategoryKey(category: ClothingCategory): String = "fixed:${category.name}"

fun customCategoryKey(name: String): String = "custom:${name.trim()}"

fun itemCategoryKey(item: ClothingEntity): String =
    item.customCategoryName
        ?.takeIf { it.isNotBlank() }
        ?.let { customCategoryKey(it) }
        ?: fixedCategoryKey(item.category)

private fun fixedCategoryOptions(): List<CategoryOption> =
    FixedClothingCategories.map { CategoryOption(fixedCategory = it) }
