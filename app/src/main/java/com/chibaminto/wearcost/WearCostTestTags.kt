package com.chibaminto.wearcost

object WearCostTestTags {
    const val StartBatchRecord = "start-batch-record"
    const val SelectionHeader = "selection-header"
    const val SelectionCancel = "selection-cancel"
    const val BatchRecordButton = "batch-record-button"
    const val AddClothingFab = "add-clothing-fab"
    const val HomeList = "home-list"

    fun clothingCard(id: Long): String = "clothing-card-$id"
    fun todayWoreButton(id: Long): String = "today-wore-button-$id"
    fun wearCostReaction(id: Long): String = "wear-cost-reaction-$id"
}
