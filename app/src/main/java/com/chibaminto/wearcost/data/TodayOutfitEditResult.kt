package com.chibaminto.wearcost.data

data class TodayOutfitEditResult(
    val operationId: String,
    val snapshots: List<WearRecordSnapshot>,
    val removedRecords: List<WearRecordEntity>,
    val addedIds: Set<Long>,
    val removedIds: Set<Long>
)

sealed interface TodayOutfitEditResultState {
    data class Success(val result: TodayOutfitEditResult) : TodayOutfitEditResultState
    data object MissingItems : TodayOutfitEditResultState
    data object AlreadyUpdating : TodayOutfitEditResultState
}
