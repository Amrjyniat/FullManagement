package com.productivity.fullmangement.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.productivity.fullmangement.data.domain.FiltersData
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.data.local.PeriodsDateFilter
import com.productivity.fullmangement.data.local.TaskType
import com.productivity.fullmangement.data.local.TaskType.*
import com.productivity.fullmangement.data.local.database.TaskLocal
import com.productivity.fullmangement.data.local.database.TasksDao
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import javax.inject.Inject

class Repository @Inject constructor(
    private val tasksDao: TasksDao,
    private val dataStoreManager: DataStoreManager,
) {

    suspend fun addTask(task: TaskLocal): Long {
        return tasksDao.saveTask(task = task)
    }
    suspend fun updateTask(task: TaskLocal): Int {
        return tasksDao.updateTask(task = task)
    }
    suspend fun archiveTask(taskId: Long): Int {
        return tasksDao.archiveTask(taskId = taskId)
    }
    suspend fun unarchiveTask(taskId: Long): Int {
        return tasksDao.unarchiveTask(taskId = taskId)
    }

    suspend fun getTaskById(taskId: Long) = tasksDao.getTaskById(taskId)

    suspend fun deleteTask(taskId: Long) = tasksDao.deleteTask(taskId)

    fun getTasksCount(query: SupportSQLiteQuery) = tasksDao.getTasksCount(query).distinctUntilChanged()

    fun getTasks(tasksType: TaskType, query: String = "", filters: FiltersData = FiltersData()) =
        if (!filters.isThereAnyFilterApplied()) {
            when (tasksType) {
                ALL -> tasksDao.getAllTasks(query)
                EXPIRATION -> tasksDao.getExpirationTasks(query)
                NEAR_EXPIRATION -> tasksDao.getNearExpirationTasks(query)
                COMPLETED -> tasksDao.getCompletedTasks(query)
                ARCHIVED -> tasksDao.getArchivedTasks(query)
            }
        } else {
            getFilteredTasks(tasksType, filters, query)
        }.distinctUntilChanged()

    private fun getFilteredTasks(taskType: TaskType, filters: FiltersData, query: String = ""): Flow<List<TaskLocal>> {
        val conditions = mutableListOf<String>()

        conditions.add("( isArchived = ${if (taskType == ARCHIVED) 1 else 0} )")
        when(taskType){
            EXPIRATION -> {
                conditions.add("( ( dueDateTime > 0 AND datetime(dueDateTime / 1000,'unixepoch') < datetime('now') ) AND (task_state_id != ${EnumTaskState.DONE.taskStateId}) )")
            }
            NEAR_EXPIRATION -> {
                conditions.add("( ( datetime(dueDateTime / 1000, 'unixepoch') BETWEEN datetime('now') AND datetime('now', '+2 day') ) AND ( task_state_id != ${EnumTaskState.DONE.taskStateId}) )")
            }
            COMPLETED -> {
                conditions.add("( task_state_id = ${EnumTaskState.DONE.taskStateId} )")
            }
        }

        with(filters) {
            if (query.isNotEmpty()){
                conditions.add(
                    "(title COLLATE NOCASE LIKE  '%' || '$query' || '%' OR description COLLATE NOCASE LIKE '%' || '$query' || '%')"
                )
            }
            if (prioritiesId.isNotEmpty()){
                conditions.add(
                    "priority_id IN (${
                        prioritiesId.toString().removeSurrounding("[", "]")
                    })"
                )
            }
            if (taskStates.isNotEmpty()){
                conditions.add(
                    "task_state_id IN (${
                        taskStates.toString().removeSurrounding("[", "]")
                    })"
                )
            }
            if (repetitions.isNotEmpty()){
                conditions.add(
                    "repetition_id IN (${
                        repetitions.toString().removeSurrounding("[", "]")
                    })"
                )
            }
            if (periodsDateFilter !is PeriodsDateFilter.Nothing){
                val dateCondition = periodsDateFilter.getDateFilterQuery()
                if (dateCondition.isNotEmpty())
                    conditions.add(dateCondition)
            }
        }

        val conditionsMerged = conditions.joinToString(separator = " AND ") { it }

        var queryString = "SELECT * FROM tasks"
        if (conditionsMerged.isNotEmpty())
            queryString += " WHERE $conditionsMerged"

        queryString += if (taskType == ALL)
            " ORDER BY CASE WHEN dueDateTime = -1 OR task_state_id = 3 then 9999 end, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))"
        else
             " ORDER BY priority_id, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))"
        val query = SimpleSQLiteQuery(queryString)
        Timber.i("TestQuery query: ${query.sql}")
        return tasksDao.getTasksFiltered(query)
    }

    private fun PeriodsDateFilter.getDateFilterQuery(): String {
        return when (this) {
            is PeriodsDateFilter.Today -> {
                "( strftime('%d', dueDateTime / 1000,'unixepoch') = strftime('%d', 'now') )"
            }
            is PeriodsDateFilter.Tomorrow -> {
                "( strftime('%d', dueDateTime / 1000,'unixepoch') = strftime('%d', 'now', '+1 day') )"
            }
            is PeriodsDateFilter.ThisWeek -> {
                "( strftime('%W', dueDateTime / 1000,'unixepoch') = strftime('%W', 'now') )"
            }
            is PeriodsDateFilter.ThisMonth -> {
                "( strftime('%m', dueDateTime / 1000,'unixepoch') = strftime('%m', 'now') )"
            }
            is PeriodsDateFilter.CustomDate -> {
                if (fromDate > 0 && toDate > 0) {
                    "( date(dueDateTime / 1000,'unixepoch') BETWEEN date(${fromDate / 1000},'unixepoch') AND date(${toDate / 1000},'unixepoch') )"
                } else if (fromDate > 0) {
                    "( date(dueDateTime / 1000,'unixepoch') > date(${fromDate / 1000},'unixepoch') )"
                } else if (toDate > 0) {
                    "( date(dueDateTime / 1000,'unixepoch') < date(${toDate / 1000},'unixepoch') )"
                } else {
                    ""
                }
            }
            is PeriodsDateFilter.Nothing -> ""
        }
    }



}