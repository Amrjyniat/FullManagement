package com.productivity.fullmangement.data.domain

import com.productivity.fullmangement.data.local.PeriodsDateFilter

data class FiltersData(
    val prioritiesId: List<Long> = listOf(),
    val taskStates: List<Long> = listOf(),
    val repetitions: List<Long> = listOf(),
    val periodsDateFilter: PeriodsDateFilter = PeriodsDateFilter.Nothing(),
){
    fun isThereAnyFilterApplied() =
        prioritiesId.isNotEmpty() ||
                taskStates.isNotEmpty() ||
                repetitions.isNotEmpty() ||
                periodsDateFilter !is PeriodsDateFilter.Nothing
}