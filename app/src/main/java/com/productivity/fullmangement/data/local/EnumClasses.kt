package com.productivity.fullmangement.data.local

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.productivity.fullmangement.R
import com.productivity.fullmangement.ui.theme.Orange

enum class EnumPriorities(val priorityId: Long, @StringRes val resIdPriorityName: Int, val color: Color) {
    HIGH(1, R.string.high, Color.Red),
    MEDIUM(2, R.string.medium, Orange),
    LOW(3, R.string.low, Color.Yellow)
}

enum class EnumTaskState(val taskStateId: Long, @StringRes val resIdTaskStateName: Int) {
    NOT_STARTED(1, R.string.not_started),
    IN_PROGRESS(2, R.string.in_progress_app),
    DONE(3, R.string.done)
}

enum class EnumRepetition(val repetitionId: Long, @StringRes val resIdRepetitionName: Int) {
    NOT_REPEATED(1, R.string.not_repeated),
    DAILY(2, R.string.daily),
    WEEKLY(3, R.string.weekly),
    MONTHLY(4, R.string.monthly),
    YEARLY(5, R.string.yearly)
}

enum class EnumLanguage(@StringRes val resStringId: Int, @StringRes val languageCode: Int){
    ARABIC(R.string.arabic, R.string.arabic_code),
    ENGLISH(R.string.english, R.string.english_code)
}

enum class EnumReminders(val timeInMills: Long){
    BEFORE_ONE_DAY(86400000),
    BEFORE_TWO_HOURS(7200000),
    WHEN_DUE_DATE(0)
}

enum class DataBaseQueries(val query: SupportSQLiteQuery){
    ALL_TASKS(SimpleSQLiteQuery("SELECT COUNT() FROM tasks WHERE isArchived = 0")),
    EXPIRATION_DATE_TASKS(SimpleSQLiteQuery("SELECT COUNT() FROM tasks WHERE (isArchived = 0) AND ( dueDateTime > 0 AND datetime(dueDateTime / 1000,'unixepoch') < datetime('now') ) AND ( task_state_id != ${EnumTaskState.DONE.taskStateId} )")),
    NEAR_EXPIRATION_TASKS(SimpleSQLiteQuery("SELECT COUNT() FROM tasks WHERE (isArchived = 0) AND ( datetime(dueDateTime / 1000, 'unixepoch') BETWEEN datetime('now') AND datetime('now', '+2 day') ) AND ( task_state_id != ${EnumTaskState.DONE.taskStateId}  )")),
    COMPLETED_TASKS(SimpleSQLiteQuery("SELECT COUNT() FROM tasks WHERE (isArchived = 0) AND (task_state_id = ${EnumTaskState.DONE.taskStateId})")),
}

enum class TaskType(val readableStringResId: Int) {
    ALL(R.string.all_tasks),
    EXPIRATION(R.string.tasks_expiration),
    NEAR_EXPIRATION(R.string.tasks_near_expiration),
    COMPLETED(R.string.tasks_completed),
    ARCHIVED(R.string.archived_tasks),
}

enum class PeriodsDateFilterEnum(@StringRes val stringResId: Int){
    TODAY(R.string.today),
    TOMORROW(R.string.tomorrow),
    THIS_WEEK(R.string.this_week),
    THIS_MONTH(R.string.this_month),
    CUSTOM_DATE(R.string.custom_date),
}

sealed class PeriodsDateFilter{
    @get:StringRes
    abstract val titleStringResId: Int

    data class Nothing(override val titleStringResId: Int = R.string.nothing): PeriodsDateFilter()
    data class Today(override val titleStringResId: Int = R.string.today): PeriodsDateFilter()
    data class Tomorrow(override val titleStringResId: Int = R.string.tomorrow): PeriodsDateFilter()
    data class ThisWeek(override val titleStringResId: Int = R.string.this_week): PeriodsDateFilter()
    data class ThisMonth(override val titleStringResId: Int = R.string.this_month): PeriodsDateFilter()
    data class CustomDate(var fromDate: Long = -1L, val toDate: Long = -1L, override val titleStringResId: Int = R.string.custom_date): PeriodsDateFilter()
}