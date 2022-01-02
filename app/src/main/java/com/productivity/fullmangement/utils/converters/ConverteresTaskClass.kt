package com.productivity.fullmangement.utils.converters

import android.content.Context
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.EnumPriorities
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.data.local.database.TaskLocal


fun TaskDomain.asLocalModel(
    dateFormat: String = EnumDateTimeFormats.NORMAL_DATE_TIME.format,
    context: Context
): TaskLocal {
    return TaskLocal(
        taskId = taskId,
        title = title,
        description = description,
        dueDateTime = dueDateTime?.getTimeInMillsFromDateFormatted(dateFormat),
        priorityId = priority.priorityId,
        taskStateId = taskState.taskStateId,
        repetitionId = repetition.repetitionId,
        isArchived = isArchived,
        isArchivedAfterCompleted = isArchivedAfterCompleted
    )
}

fun TaskLocal.asDomainModel(
    context: Context,
    dateFormat: String = EnumDateTimeFormats.NORMAL_DATE_TIME.format
): TaskDomain {
    return TaskDomain(
        taskId = taskId,
        title = title,
        description = description,
        dueDateTime = dueDateTime?.getDateFormattedFromMills(dateFormat),
        priority = EnumPriorities.values().single { it.priorityId == priorityId },
        taskState = EnumTaskState.values().single { it.taskStateId == taskStateId },
        repetition = EnumRepetition.values().single { it.repetitionId == repetitionId },
        isArchived = isArchived,
        isArchivedAfterCompleted = isArchivedAfterCompleted
    )
}

fun List<TaskLocal>.asDomainModel(
    context: Context,
    dateFormat: String = EnumDateTimeFormats.NORMAL_DATE_TIME.format
): List<TaskDomain> {
    return map { taskLocal ->
        TaskDomain(
            taskId = taskLocal.taskId,
            title = taskLocal.title,
            description = taskLocal.description,
            dueDateTime = taskLocal.dueDateTime?.getDateFormattedFromMills(dateFormat),
            priority = EnumPriorities.values().single { it.priorityId == taskLocal.priorityId },
            taskState =  EnumTaskState.values().single { it.taskStateId == taskLocal.taskStateId },
            repetition = EnumRepetition.values().single { it.repetitionId == taskLocal.repetitionId },
            isArchived = taskLocal.isArchived,
            isArchivedAfterCompleted = taskLocal.isArchivedAfterCompleted
        )
    }
}


