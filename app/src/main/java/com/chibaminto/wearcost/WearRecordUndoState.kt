package com.chibaminto.wearcost

import com.chibaminto.wearcost.data.WearRecordSnapshot
import com.chibaminto.wearcost.data.TodayOutfitEditResult

sealed interface WearUndoState {
    val operationId: String

    data class Single(
        val snapshot: WearRecordSnapshot
    ) : WearUndoState {
        override val operationId: String = snapshot.operationId
    }

    data class Batch(
        val snapshots: List<WearRecordSnapshot>
    ) : WearUndoState {
        override val operationId: String = snapshots.firstOrNull()?.operationId.orEmpty()
    }

    data class TodayOutfitEdit(
        val result: TodayOutfitEditResult
    ) : WearUndoState {
        override val operationId: String = result.operationId
    }
}

fun shouldUndoWear(
    activeSnapshot: WearUndoState?,
    requestedOperationId: String
): Boolean =
    activeSnapshot?.operationId == requestedOperationId
