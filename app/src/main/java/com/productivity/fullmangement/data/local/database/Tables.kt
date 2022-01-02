package com.productivity.fullmangement.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.productivity.fullmangement.utils.getLocalDateFromUTC

@Entity(tableName = "tasks")
data class TaskLocal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "task_id") val taskId: Long = 0L,
    val title: String,
    val description: String?,
    val dueDateTime: Long?,
    @ColumnInfo(name = "priority_id") val priorityId: Long,
    @ColumnInfo(name = "task_state_id") val taskStateId: Long,
    @ColumnInfo(name = "repetition_id") val repetitionId: Long,
    val isArchived: Boolean = false,
    val isArchivedAfterCompleted: Boolean = false
){
    fun getLocalDueDateInMills(): Long?{
        return if (dueDateTime != null)
            getLocalDateFromUTC(dueDateTime)
        else
            null
    }
}

/*@Entity(tableName = "priorities")
data class Priority(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "priority_en") var priorityEn: String,
    @ColumnInfo(name = "priority_ar") var priorityAr: String,
    @Ignore val color: Color = Color.Unspecified,
    @Ignore val _priority: String = if (FullManagementApplication.getCurrLanguage() == EnumLanguage.ARABIC) priorityAr else priorityEn
){
    constructor() : this(0L, "", "")
    @Ignore var priority = _priority
        private set

    companion object {
        val medium = Priority(
            EnumPriorities.MEDIUM.priorityId,
            FullManagementApplication().getContext().getResStringLanguage(R.string.medium, "en"),
            FullManagementApplication().getContext().getResStringLanguage(R.string.medium, "ar"),
            Orange
        )

        val PREPOPULATE_PRIORITIES_DATA = listOf(
            Priority(
                EnumPriorities.HIGH.priorityId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.high, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.high, "ar"),
                Color.Red
            ),
            Priority(
                EnumPriorities.MEDIUM.priorityId,
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.medium, "en"),
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.medium, "ar"),
                Orange
            ),
            Priority(
                EnumPriorities.LOW.priorityId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.low, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.low, "ar"),
                Color.Yellow
            )
        )
    }
}


@Entity(tableName = "task_states")
data class TaskState(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "TaskState_en") var taskStateEn: String,
    @ColumnInfo(name = "TaskState_ar") var taskStateAr: String,
    @Ignore val _TaskState: String = if (FullManagementApplication.getCurrLanguage() == EnumLanguage.ARABIC) taskStateAr else taskStateEn
){
    constructor() : this(0L, "", "")
    @Ignore var taskState = _TaskState
        private set

    companion object {
        val notStartedState = TaskState(
            EnumTaskState.NOT_STARTED.taskStateId,
            FullManagementApplication().getContext().getResStringLanguage(R.string.not_started, "en"),
            FullManagementApplication().getContext().getResStringLanguage(R.string.not_started, "ar"),
        )
        val doneState = TaskState(
            EnumTaskState.DONE.taskStateId,
            FullManagementApplication().getContext().getResStringLanguage(R.string.done, "en"),
            FullManagementApplication().getContext().getResStringLanguage(R.string.done, "ar")
        )

        val PREPOPULATE_TASK_STATES_DATA = listOf(
            TaskState(
                EnumTaskState.NOT_STARTED.taskStateId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.not_started, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.not_started, "ar")
            ),
            TaskState(
                EnumTaskState.IN_PROGRESS.taskStateId,
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.in_progress_app, "en"),
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.in_progress_app, "ar")
            ),
            TaskState(
                EnumTaskState.DONE.taskStateId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.done, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.done, "ar")
            )
        )
    }
}


@Entity(tableName = "repetitions")
data class Repetition(
    @PrimaryKey var id: Long,
    @ColumnInfo(name = "repetition_en") var repetitionEn: String,
    @ColumnInfo(name = "repetition_ar") var repetitionAr: String,
    @Ignore val _repetition: String = if (FullManagementApplication.getCurrLanguage() == EnumLanguage.ARABIC) repetitionAr else repetitionEn
){
    constructor() : this(0L, "", "")
    @Ignore var repetition = _repetition
        private set

    companion object {
        val notRepeated = Repetition(
            EnumRepetition.NOT_REPEATED.repetitionId,
            FullManagementApplication().getContext().getResStringLanguage(R.string.not_repeated, "en"),
            FullManagementApplication().getContext().getResStringLanguage(R.string.not_repeated, "ar")
        )

        val PREPOPULATE_REPETITIONS_DATA = listOf(
            Repetition(
                EnumRepetition.NOT_REPEATED.repetitionId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.not_repeated, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.not_repeated, "ar")
            ),
            Repetition(
                EnumRepetition.DAILY.repetitionId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.daily, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.daily, "ar")
            ),
            Repetition(
                EnumRepetition.WEEKLY.repetitionId,
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.weekly, "en"),
                FullManagementApplication().getContext()
                    .getResStringLanguage(R.string.weekly, "ar")
            ),
            Repetition(
                EnumRepetition.MONTHLY.repetitionId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.monthly, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.monthly, "ar")
            ),
            Repetition(
                EnumRepetition.YEARLY.repetitionId,
                FullManagementApplication().getContext().getResStringLanguage(R.string.yearly, "en"),
                FullManagementApplication().getContext().getResStringLanguage(R.string.yearly, "ar")
            )
        )
    }
}*/
