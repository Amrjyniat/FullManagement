package com.productivity.fullmangement.data.domain

import com.productivity.fullmangement.data.local.EnumPriorities
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.utils.converters.EnumDateTimeFormats
import com.productivity.fullmangement.utils.converters.getTimeInMillsFromDateFormatted
import java.util.*


data class TaskDomain(
    val taskId: Long = 0L,
    val title: String,
    val description: String?,
    val dueDateTime: String?,
    val priority: EnumPriorities,
    val taskState: EnumTaskState,
    val repetition: EnumRepetition,
    val isArchived: Boolean = false,
    val isArchivedAfterCompleted: Boolean = false
){

    val isExpirationDate = Date().time > dueDateTime?.getTimeInMillsFromDateFormatted(EnumDateTimeFormats.SHOW_DATE_TIME.format) ?: Int.MAX_VALUE.toLong() && taskState != EnumTaskState.DONE

    companion object {
        val Empty = TaskDomain(
            title = "", description = null, dueDateTime = null, priority = EnumPriorities.MEDIUM, taskState = EnumTaskState.NOT_STARTED, repetition = EnumRepetition.NOT_REPEATED
        )
        val Preview = TaskDomain(
            title = "Test this title in the task",
            description = "Put here some description which showing the status in the preview",
            dueDateTime = "12-10-2021\n10:30",
            priority = EnumPriorities.MEDIUM,
            taskState = EnumTaskState.NOT_STARTED,
            repetition = EnumRepetition.NOT_REPEATED,
        )
    }
}