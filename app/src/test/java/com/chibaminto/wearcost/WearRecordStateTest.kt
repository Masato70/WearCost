package com.chibaminto.wearcost

import com.chibaminto.wearcost.data.WearRecordSnapshot
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WearRecordStateTest {
    @Test
    fun undoOnlyAppliesToActiveLatestSnapshot() {
        val first = snapshot(operationId = "wear-a", clothingId = 1)
        val second = snapshot(operationId = "wear-b", clothingId = 2)

        assertFalse(
            shouldUndoWear(
                activeSnapshot = WearUndoState.Single(second),
                requestedOperationId = first.operationId
            )
        )
        assertTrue(
            shouldUndoWear(
                activeSnapshot = WearUndoState.Single(second),
                requestedOperationId = second.operationId
            )
        )
    }

    @Test
    fun snackbarTimeoutClearsUndoTarget() {
        val snapshot = snapshot(operationId = "expired", clothingId = 1)

        assertFalse(shouldUndoWear(activeSnapshot = null, requestedOperationId = snapshot.operationId))
    }

    @Test
    fun batchUndoIsDistinguishedByOperationId() {
        val batch = WearUndoState.Batch(
            listOf(
                snapshot(operationId = "batch", clothingId = 1),
                snapshot(operationId = "batch", clothingId = 2),
                snapshot(operationId = "batch", clothingId = 3)
            )
        )
        val newerSingle = WearUndoState.Single(snapshot(operationId = "single-new", clothingId = 4))

        assertTrue(shouldUndoWear(batch, "batch"))
        assertFalse(shouldUndoWear(newerSingle, "batch"))
    }

    @Test
    fun updateGuardRejectsSameItemWhileInFlightButAllowsDifferentItems() {
        val guard = WearUpdateGuard()

        assertTrue(guard.tryStart(1))
        assertFalse(guard.tryStart(1))
        assertTrue(guard.tryStart(2))

        guard.finish(1)
        assertTrue(guard.tryStart(1))
    }

    @Test
    fun updateGuardRejectsBatchWhenAnyItemIsAlreadyUpdating() {
        val guard = WearUpdateGuard()

        assertTrue(guard.tryStart(1))
        assertFalse(guard.tryStartAll(setOf(1, 2, 3)))

        guard.finish(1)
        assertTrue(guard.tryStartAll(setOf(1, 2, 3)))
        assertFalse(guard.tryStart(2))

        guard.finishAll(setOf(1, 2, 3))
        assertTrue(guard.tryStart(2))
    }

    @Test
    fun stateIsKeyedByClothingIdNotListPosition() {
        val guard = WearUpdateGuard()
        val firstOrder = listOf(1L, 2L)
        val secondOrder = listOf(2L, 1L)

        assertTrue(guard.tryStart(firstOrder.first()))
        assertTrue(firstOrder.first() in guard.updatingItemIds.value)
        assertTrue(secondOrder.last() in guard.updatingItemIds.value)
        assertFalse(secondOrder.first() in guard.updatingItemIds.value)
    }

    private fun snapshot(operationId: String, clothingId: Long): WearRecordSnapshot =
        WearRecordSnapshot(
            operationId = operationId,
            clothingId = clothingId,
            wearCount = 1,
            lastWornDateEpochDay = null,
            lastWearRecordedAtMillis = null,
            updatedAtMillis = 1000
        )
}
