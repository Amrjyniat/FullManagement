package com.productivity.fullmangement.utils

import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.EnumPriorities
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState


val tasksForPreview = listOf(
    TaskDomain(
        taskId = 128,
        title = "Create the swappable library for compose test for test fo that for test what about this problem that for test what about this problem",
        description = null,
        dueDateTime = "2021/11/15\n22:45",
        priority = EnumPriorities.MEDIUM,
        taskState = EnumTaskState.NOT_STARTED,
        repetition = EnumRepetition.NOT_REPEATED,
    ),
    TaskDomain(
        taskId = 55,
        title = "Create the swappable library for compose test for test fo that for test what about this problem that for test what about this problem",
        description = null,
        dueDateTime = "2021/11/10\n12:10",
        priority = EnumPriorities.HIGH,
        taskState = EnumTaskState.NOT_STARTED,
        repetition = EnumRepetition.YEARLY,
    ),
    TaskDomain(
        taskId = 25,
        title = "Create the swappable library for compose test for test fo that for test what about this problem that for test what about this problem",
        description = null,
        dueDateTime = "2021/11/15\n8:30",
        priority = EnumPriorities.LOW,
        taskState = EnumTaskState.IN_PROGRESS,
        repetition = EnumRepetition.DAILY,
    ),
    TaskDomain(
        taskId = 7,
        title = "Create the swappable library for compose test for test fo that for test what about this problem that for test what about this problem",
        description = null,
        dueDateTime = "2021/12/05\n5:00",
        priority = EnumPriorities.MEDIUM,
        taskState = EnumTaskState.IN_PROGRESS,
        repetition = EnumRepetition.NOT_REPEATED,
    ),
)