package com.productivity.fullmangement.utils.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.EnumReminders
import com.productivity.fullmangement.data.local.database.TaskLocal
import com.productivity.fullmangement.data.local.database.TasksDao
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.data.local.datastore.SharedPrefValue
import com.productivity.fullmangement.utils.getProperTimeInMillsForAlarmManager
import com.productivity.fullmangement.utils.getResStringLanguage
import com.productivity.fullmangement.utils.sendNotification
import com.productivity.fullmangement.utils.setAlarm
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver: BroadcastReceiver() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var tasksDao: TasksDao

    override fun onReceive(context: Context, intent: Intent) {
        val ordinal = intent.extras?.getInt("enumReminderOrdinal") ?: -1
        val enumReminder = EnumReminders.values().getOrNull(ordinal)
        val taskId = intent.extras?.getLong("taskId") ?: -1
        Timber.i("TestAlarm onReceive() taskId: $taskId , ordinal: $ordinal, enumReminder: $enumReminder}")

        if (taskId < 0 || enumReminder == null)
            return

        CoroutineScope(Dispatchers.IO).launch {
            val task = tasksDao.getTaskById(taskId)

            val currLangCode = dataStoreManager.readValue(
                SharedPrefKey.selectedAppLanguage,
                SharedPrefValue.DEFAULT_APP_LANGUAGE
            ).first()

            var title = ""
            var desc = ""
            when (enumReminder) {
                EnumReminders.BEFORE_ONE_DAY -> {
                    title = context.getResStringLanguage(
                        R.string.notification_title_before_one_day,
                        currLangCode,
                        taskId
                    )
                    desc = context.getResStringLanguage(
                        R.string.notification_description_before_one_day,
                        currLangCode,
                        task.title
                    )
                }
                EnumReminders.BEFORE_TWO_HOURS -> {
                    title = context.getResStringLanguage(
                        R.string.notification_title_before_two_hours,
                        currLangCode,
                        taskId
                    )
                    desc = context.getResStringLanguage(
                        R.string.notification_description_before_two_hours,
                        currLangCode,
                        task.title
                    )
                }
                EnumReminders.WHEN_DUE_DATE -> {
                    title = context.getResStringLanguage(
                        R.string.notification_title_when_due_date,
                        currLangCode,
                        taskId
                    )
                    desc = context.getResStringLanguage(
                        R.string.notification_description_when_due_date,
                        currLangCode,
                        task.title
                    )
                }
            }

            sendNotification(title, desc, taskId, context)

            setNextAlarm(task, enumReminder, context)
        }

    }

    private suspend fun setNextAlarm(task: TaskLocal, enumReminder: EnumReminders, context: Context) {
        val whenDueDate = dataStoreManager.readValue(
                SharedPrefKey.isRemindWhenDueDate,
                SharedPrefValue.DEFAULT_IS_REMIND_WHEN_DUE_DATE
            ).first()

        val beforeTwoHours = dataStoreManager.readValue(
                SharedPrefKey.isRemindBeforeTwoHours,
                SharedPrefValue.DEFAULT_IS_REMIND_BEFORE_TWO_HOURS
            ).first()

        val beforeTowHoursInMills = task.getLocalDueDateInMills()?.getProperTimeInMillsForAlarmManager(EnumReminders.BEFORE_TWO_HOURS.timeInMills) ?: return
        val whenDueDateInMills = task.getLocalDueDateInMills()?.getProperTimeInMillsForAlarmManager(EnumReminders.WHEN_DUE_DATE.timeInMills) ?: return

        if (enumReminder == EnumReminders.BEFORE_ONE_DAY && beforeTwoHours){
            Timber.i("TestAlarm setNextAlarm() beforeTwoHours Yes")
            setAlarm(task.taskId, beforeTowHoursInMills, EnumReminders.BEFORE_TWO_HOURS, context)
        } else if (enumReminder == EnumReminders.BEFORE_TWO_HOURS && whenDueDate){
            Timber.i("TestAlarm setNextAlarm() whenDueDate Yes")
            setAlarm(task.taskId, whenDueDateInMills, EnumReminders.WHEN_DUE_DATE, context)
        }


    }

}