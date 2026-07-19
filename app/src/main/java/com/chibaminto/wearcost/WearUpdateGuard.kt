package com.chibaminto.wearcost

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class WearUpdateGuard {
    private val _updatingItemIds = MutableStateFlow<Set<Long>>(emptySet())
    val updatingItemIds: StateFlow<Set<Long>> = _updatingItemIds

    fun tryStart(itemId: Long): Boolean {
        var marked = false
        _updatingItemIds.update { current ->
            if (itemId in current) {
                current
            } else {
                marked = true
                current + itemId
            }
        }
        return marked
    }

    fun finish(itemId: Long) {
        _updatingItemIds.update { it - itemId }
    }

    fun tryStartAll(itemIds: Set<Long>): Boolean {
        if (itemIds.isEmpty()) return false
        var marked = false
        _updatingItemIds.update { current ->
            if (itemIds.any { it in current }) {
                current
            } else {
                marked = true
                current + itemIds
            }
        }
        return marked
    }

    fun finishAll(itemIds: Set<Long>) {
        _updatingItemIds.update { it - itemIds }
    }
}
