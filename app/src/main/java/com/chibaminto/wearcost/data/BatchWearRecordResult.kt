package com.chibaminto.wearcost.data

sealed interface BatchWearRecordResult {
    data class Success(
        val snapshots: List<WearRecordSnapshot>
    ) : BatchWearRecordResult

    data object MissingItems : BatchWearRecordResult
    data object AlreadyUpdating : BatchWearRecordResult
}
