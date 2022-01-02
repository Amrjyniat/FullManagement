package com.productivity.fullmangement.data.local.database

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.productivity.fullmangement.data.local.EnumTaskState

import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveTask(task: TaskLocal): Long

    @Update
    suspend fun updateTask(task: TaskLocal): Int

    @RawQuery(observedEntities = [TaskLocal::class])
    fun getTasksCount(query: SupportSQLiteQuery): Flow<Int>

    @Query("SELECT * FROM tasks WHERE task_Id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskLocal

    @Query("SELECT * FROM tasks WHERE (isArchived = 0) AND (title COLLATE NOCASE LIKE '%' || :query || '%' OR description COLLATE NOCASE LIKE '%' || :query || '%') ORDER BY CASE WHEN dueDateTime = -1 OR task_state_id = 3 then 9999 end, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now')), case when priority_id = 1 then 1 when 2 then 2 end, case when task_state_id = 2 then 1 when 1 then 2 end")//ORDER BY datetime('now') > datetime(dueDateTime / 1000, 'unixepoch') desc")// ORDER BY CASE task_state_id WHEN 2 THEN 1 WHEN 1 THEN 2 WHEN 3 THEN 3 END, dueDateTime   //abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))
    fun getAllTasks(query: String, ): Flow<List<TaskLocal>>

    @RawQuery(observedEntities = [TaskLocal::class])
    fun getTasksFiltered(query: SupportSQLiteQuery): Flow<List<TaskLocal>>

    @Query("SELECT * FROM tasks WHERE (isArchived = 0) AND ( dueDateTime > 0 AND datetime(dueDateTime / 1000,'unixepoch') < datetime('now') ) AND ( task_state_id != :taskStateId ) AND (title COLLATE NOCASE LIKE '%' || :query || '%' OR description COLLATE NOCASE LIKE '%' || :query || '%') ORDER BY priority_id, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))")
    fun getExpirationTasks(
        query: String,
        taskStateId: Long = EnumTaskState.DONE.taskStateId
    ): Flow<List<TaskLocal>>

    @Query("SELECT * FROM tasks WHERE (isArchived = 0) AND ( datetime(dueDateTime / 1000, 'unixepoch') BETWEEN datetime('now') AND datetime('now', '+2 day') ) AND ( task_state_id != :taskStateId ) AND (title COLLATE NOCASE LIKE '%' || :query || '%' OR description COLLATE NOCASE LIKE '%' || :query || '%') ORDER BY priority_id, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))")
    fun getNearExpirationTasks(
        query: String,
        taskStateId: Long = EnumTaskState.DONE.taskStateId
    ): Flow<List<TaskLocal>>

    @Query("SELECT * FROM tasks where (isArchived = 0) AND (task_state_id = :taskStateId) AND (title COLLATE NOCASE LIKE '%' || :query || '%' OR description COLLATE NOCASE LIKE '%' || :query || '%') ORDER BY priority_id, abs(strftime('%s', dueDateTime / 1000,'unixepoch') - strftime('%s', 'now'))")
    fun getCompletedTasks(
        query: String,
        taskStateId: Long = EnumTaskState.DONE.taskStateId
    ): Flow<List<TaskLocal>>

    @Query("SELECT * FROM tasks where (isArchived = 1) AND (title COLLATE NOCASE LIKE '%' || :query || '%' OR description COLLATE NOCASE LIKE '%' || :query || '%')")
    fun getArchivedTasks(query: String): Flow<List<TaskLocal>>

    @Query("DELETE FROM tasks WHERE task_Id = :taskId")
    suspend fun deleteTask(taskId: Long): Int

    @Query("UPDATE tasks SET isArchived = 1 WHERE task_Id = :taskId")
    suspend fun archiveTask(taskId: Long): Int

    @Query("UPDATE tasks SET isArchived = 0 WHERE task_Id = :taskId")
    suspend fun unarchiveTask(taskId: Long): Int

/*    @Insert
    suspend fun insertPriorities(priorities: List<Priority>)

    @Insert
    suspend fun insertTaskStates(taskStates: List<TaskState>)

    @Insert
    suspend fun insertRepetitions(repetitions: List<Repetition>)*/

}