package com.productivity.fullmangement.ui.main.add_edit_task

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.domain.TaskDomain
import com.productivity.fullmangement.data.local.EnumPriorities
import com.productivity.fullmangement.data.local.EnumReminders
import com.productivity.fullmangement.data.local.EnumRepetition
import com.productivity.fullmangement.data.local.EnumTaskState
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.data.local.datastore.SharedPrefValue
import com.productivity.fullmangement.data.repositories.Repository
import com.productivity.fullmangement.utils.*
import com.productivity.fullmangement.utils.converters.*
import com.productivity.fullmangement.utils.notifications.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.util.*
import javax.inject.Inject


@HiltViewModel
class AddTaskViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: Repository,
    private val dataStoreManager: DataStoreManager,
    private val args: SavedStateHandle
) : ViewModel() {

    val taskId = args.get<Long>("taskId") ?: -1

    val taskName = MutableStateFlow("")
    fun onTaskNameChanged(newValue: String) {
        taskName.value = newValue
    }

    val errorTaskNameResId = taskName.mapLatest {
        it.validate(R.string.min_title_length_is_five_char) { taskName ->
            taskName.length > 5
        }
    }.distinctUntilChanged().debounce(700)

    val taskDesc = MutableStateFlow("")
    fun onTaskDescChanged(newValue: String) {
        taskDesc.value = newValue
    }

    val dueDateTime = MutableStateFlow("")
    fun setEndDate(dateTimeInMill: Long) {
        dueDateTime.value = dateTimeInMill.getDateFormattedFromMills(
            EnumDateTimeFormats.NORMAL_DATE_TIME.format
        ).orEmpty()
    }

    val priority = MutableStateFlow(EnumPriorities.MEDIUM)
    fun setPriority(priority: EnumPriorities) {
        this.priority.value = priority
    }

    val taskState = MutableStateFlow(EnumTaskState.NOT_STARTED)
    fun setTaskState(taskState: EnumTaskState) {
        this.taskState.value = taskState
    }

    val _repetition = MutableStateFlow(EnumRepetition.NOT_REPEATED)
    val repetition: StateFlow<EnumRepetition> = _repetition
    fun setRepetition(repetition: EnumRepetition) {
        this._repetition.value = repetition
    }

    val isArchivedAfterCompleted = MutableStateFlow(false)
    fun onChangeArchivedAfterCompleted() {
        isArchivedAfterCompleted.value = !isArchivedAfterCompleted.value
    }

    val isLoading = MutableStateFlow(false)
    fun setLoading(state: Boolean){ isLoading.value = state }

    val addEditTaskUiState = combine9(
        taskName,
        taskDesc,
        dueDateTime,
        priority,
        taskState,
        repetition,
        isArchivedAfterCompleted,
        isLoading,
        errorTaskNameResId
    ) { name, desc, dueDate, priority, taskState, repetition, isArchivedAfterCompleted, isLoading, errorTaskNameResId ->
        AddEditTaskUiState(
            taskId = taskId,
            name = name,
            description = desc,
            dueDate = dueDate,
            priority = priority,
            taskState = taskState,
            repetition = repetition,
            isArchivedAfterCompleted = isArchivedAfterCompleted,
            isLoading = isLoading,
            errorTaskNameResId = errorTaskNameResId
        )

    }


    init {
        if (taskId > 0){
            viewModelScope.launch {
                val taskDomain = repository.getTaskById(taskId).asDomainModel(context)

                taskDomain.apply {
                    onTaskNameChanged(title)
                    onTaskDescChanged(description.orEmpty())
                    if (dueDateTime?.isNotEmpty() == true) {
                        setEndDate(dueDateTime.getTimeInMillsFromDateFormatted(EnumDateTimeFormats.NORMAL_DATE_TIME.format))
                    }
                    setPriority(priority)
                    setTaskState(taskState)
                    setRepetition(repetition)
                    if (isArchivedAfterCompleted)
                        onChangeArchivedAfterCompleted()

                }

            }
        }
    }

    private val _navigateBack = Channel<Long>()
    val navigateBack = _navigateBack.receiveAsFlow()
    fun onNavigateBack(createdTaskId: Long) = viewModelScope.launch {
        _navigateBack.send(createdTaskId)
    }

    val currLangCode = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.selectedAppLanguage,
            SharedPrefValue.DEFAULT_APP_LANGUAGE
        ).first()
    }

    fun onSaveClicked() {
        viewModelScope.launch {
            if (taskName.value.isEmpty()) {
                context.showToast(
                    context.getResStringLanguage(R.string.please_fill_the_required_fields, currLangCode)
                )
                return@launch
            }

            if (taskId > 0){
                updateTask()
            } else {
                addTask()
            }
        }
    }

    private suspend fun updateTask() {
        setLoading(true)
        val updatedTasksCount = repository.updateTask(
            TaskDomain(
                taskId = taskId,
                title = taskName.value,
                description = taskDesc.value,
                dueDateTime = dueDateTime.value,
                priority = priority.value,
                taskState = taskState.value,
                repetition = repetition.value,
                isArchived = isArchivedAfterCompleted.value && taskState.value == EnumTaskState.DONE,
                isArchivedAfterCompleted = isArchivedAfterCompleted.value
            ).asLocalModel(context = context)
        )
        setLoading(false)
        if (updatedTasksCount > 0){
            manageScheduledNotifications(taskId)
            context.showToast(
                context.getResStringLanguage(R.string.the_task_updated_successfully, currLangCode)
            )
            onNavigateBack(taskId)
        }
    }

    private suspend fun addTask(){
        setLoading(true)
        val createdTaskId = repository.addTask(
            TaskDomain(
                title = taskName.value,
                description = taskDesc.value,
                dueDateTime = dueDateTime.value,
                priority = priority.value,
                taskState = taskState.value,
                repetition = repetition.value,
                isArchivedAfterCompleted = isArchivedAfterCompleted.value
            ).asLocalModel(context = context)
        )
        setLoading(false)
        if (createdTaskId > 0){
            manageScheduledNotifications(createdTaskId)
            context.showToast(
                context.getResStringLanguage(R.string.the_task_added_successfully, currLangCode)
            )
            onNavigateBack(createdTaskId)
        }
    }

    private val beforeOneDay = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.isRemindBeforeOneDay,
            SharedPrefValue.DEFAULT_IS_REMIND_BEFORE_ONE_DAY
        ).first()
    }
    private val beforeTwoHours = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.isRemindBeforeTwoHours,
            SharedPrefValue.DEFAULT_IS_REMIND_BEFORE_TWO_HOURS
        ).first()
    }
    private val whenDueDate = runBlocking(Dispatchers.IO) {
        dataStoreManager.readValue(
            SharedPrefKey.isRemindWhenDueDate,
            SharedPrefValue.DEFAULT_IS_REMIND_WHEN_DUE_DATE
        ).first()
    }

    private fun manageScheduledNotifications(id: Long) {
        if (dueDateTime.value.isNotEmpty() && taskState.value != EnumTaskState.DONE) {
            setAlarmNotification(dueDateTime.value, id)
        } else if (taskState.value == EnumTaskState.DONE) {
            checkAndCancelAlarmManager(id)
        }
    }

    private fun checkAndCancelAlarmManager(id: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val notifyIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("taskId", id)
        }

        val pendingIntent =  PendingIntent.getBroadcast(
            context,
            id.toInt(),
            notifyIntent,
            PendingIntent.FLAG_NO_CREATE
        )

        val isAlarmRunning = pendingIntent != null

        Timber.i("TestAlarmEx isAlarmRunning: $isAlarmRunning")

        if (isAlarmRunning) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun setAlarmNotification(dueDateTime: String, id: Long) {
        val dateTimeInMill = dueDateTime.getTimeInMillsFromDateFormatted(
            EnumDateTimeFormats.NORMAL_DATE_TIME.format,
            TimeZone.getDefault()
        )
        val diffCurrAndDueDateInMill = dateTimeInMill - Date().time

        val notifyBeforeOneDayMills = dateTimeInMill.getProperTimeInMillsForAlarmManager(EnumReminders.BEFORE_ONE_DAY.timeInMills)
        val notifyBeforeTwoHoursMills = dateTimeInMill.getProperTimeInMillsForAlarmManager(EnumReminders.BEFORE_TWO_HOURS.timeInMills)
        val notifyWhenDueDateMills = dateTimeInMill.getProperTimeInMillsForAlarmManager(EnumReminders.WHEN_DUE_DATE.timeInMills)

        Timber.i("TestAlarm setAlarmNotification() notifyBeforeOneDayMills: $notifyBeforeOneDayMills , notifyBeforeTwoHoursMills: $notifyBeforeTwoHoursMills , notifyWhenDueDateMills: $notifyWhenDueDateMills")

        if (beforeOneDay && diffCurrAndDueDateInMill >= EnumReminders.BEFORE_ONE_DAY.timeInMills) {
            Timber.i("TestAlarm setAlarmNotification() beforeOneDay Yes")
            setAlarm(id, notifyBeforeOneDayMills, EnumReminders.BEFORE_ONE_DAY, context)
        } else if (beforeTwoHours && diffCurrAndDueDateInMill >= EnumReminders.BEFORE_TWO_HOURS.timeInMills) {
            Timber.i("TestAlarm setAlarmNotification() beforeTwoHours Yes")
            setAlarm(id, notifyBeforeTwoHoursMills, EnumReminders.BEFORE_TWO_HOURS, context)
        } else if (whenDueDate && diffCurrAndDueDateInMill > EnumReminders.WHEN_DUE_DATE.timeInMills) {
            Timber.i("TestAlarm setAlarmNotification() whenDueDate Yes")
            setAlarm(id, notifyWhenDueDateMills, EnumReminders.WHEN_DUE_DATE, context)
        }

    }
}

@Immutable
data class AddEditTaskUiState(
    val taskId: Long = -1,
    val name: String = "",
    val description: String = "",
    val dueDate: String = "",
    val priority: EnumPriorities = EnumPriorities.MEDIUM,
    val taskState: EnumTaskState = EnumTaskState.NOT_STARTED,
    val repetition: EnumRepetition = EnumRepetition.NOT_REPEATED,
    val isArchivedAfterCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val errorTaskNameResId: Int? = null
){
    companion object {
        val Empty = AddEditTaskUiState()
    }
}