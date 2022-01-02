package com.productivity.fullmangement.utils

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Resources
import android.os.SystemClock
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.AlarmManagerCompat
import androidx.core.os.ConfigurationCompat
import com.google.android.play.core.review.ReviewManagerFactory
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.*
import com.productivity.fullmangement.utils.notifications.AlarmReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import java.util.*

//Get activity from context
fun Context.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}

fun Context.getResStringLanguage(id: Int, lang: String?, vararg formatArgs: Any = emptyArray()): String {
    //Get default locale to back it
    val conf = resources.configuration
    val savedLocale = conf.locale
    //Retrieve resources from desired locale
    val confAr = resources.configuration
    confAr.locale = Locale(lang)
    val metrics = DisplayMetrics()
    val resources = Resources(assets, metrics, confAr)
    //Get string which you want
    val string: String = resources.getString(id, *formatArgs)
    //Restore default locale
    conf.locale = savedLocale
    resources.updateConfiguration(conf, null)
    //return the string that you want
    return string
}

fun Context.showToast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}


fun String.validate(errorResStringId: Int, validator: (String) -> Boolean): Int? {
    return if (validator(this) || this.isEmpty()) null else errorResStringId
}


fun List<String>.getPriIdsFromNames(context: Context): List<Long>{
    return EnumPriorities.values().toList().intersect2(this){ priority, selectedPriName ->
        selectedPriName == context.getString(priority.resIdPriorityName)
    }.map { it.priorityId }
}

fun List<Long>.getPriNamesFromIds(context: Context): List<String>{
    return EnumPriorities.values().toList().intersect2(this){ priority, selectedPriName ->
        selectedPriName == priority.priorityId
    }.map { context.getString(it.resIdPriorityName) }
}

fun List<String>.getTaskStateIdsFromNames(context: Context): List<Long>{
    return EnumTaskState.values().toList().intersect2(this){ taskState, selectedTaskStateName ->
        selectedTaskStateName == context.getString(taskState.resIdTaskStateName)
    }.map { it.taskStateId }
}

fun List<Long>.getTaskStateNamesFromIds(context: Context): List<String>{
    return EnumTaskState.values().toList().intersect2(this){ taskState, selectedTaskStateName ->
        selectedTaskStateName == taskState.taskStateId
    }.map { context.getString(it.resIdTaskStateName) }
}

fun List<String>.getRepetitionIdsFromNames(context: Context): List<Long>{
    return EnumRepetition.values().toList().intersect2(this){ repetition, selectedRepetitionName ->
        selectedRepetitionName == context.getString(repetition.resIdRepetitionName)
    }.map { it.repetitionId }
}

fun List<Long>.getRepetitionNamesFromIds(context: Context): List<String>{
    return EnumRepetition.values().toList().intersect2(this){ repetition, selectedRepetitionName ->
        selectedRepetitionName == repetition.repetitionId
    }.map { context.getString(it.resIdRepetitionName) }
}

fun <T, U> List<T>.intersect2(uList: List<U>, filterPredicate : (T, U) -> Boolean) = filter { m -> uList.any { filterPredicate(m, it)} }


fun addOrRemoveFromList(
    prioritiesFilter: MutableList<String>,
    selectedVal: String
): List<String> {
    if (prioritiesFilter.contains(selectedVal)) {
        prioritiesFilter.remove(selectedVal)
    } else {
        prioritiesFilter.add(selectedVal)
    }
    return prioritiesFilter
}

fun Boolean.toInteger() = if (this) 1 else 0
fun Int.toBoolean() = this == 1

fun Long.getProperTimeInMillsForAlarmManager(timeToMinusInMills: Long) =
    (this - Date().time - timeToMinusInMills) + (SystemClock.elapsedRealtime())

fun setAlarm(taskId: Long, notifyMills: Long, enumReminder: EnumReminders, context: Context){
    Timber.i("TestAlarm setAlarm() notifyMills: $notifyMills")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notifyIntent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("taskId", taskId)
        putExtra("enumReminderOrdinal", enumReminder.ordinal)
    }

    val notifyPendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    AlarmManagerCompat.setExactAndAllowWhileIdle(
        alarmManager,
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        notifyMills,
        notifyPendingIntent
    )
}

fun getLocalDateFromUTC(dateUTCInMills: Long): Long {
    val tz = TimeZone.getDefault()
    val offset = tz.getOffset(dateUTCInMills).toLong()
    Timber.i("TestAlarm offset: $offset")
    return dateUTCInMills - offset
}

fun Context.getCurrLanguage(): EnumLanguage {
    val lang = ConfigurationCompat.getLocales(resources.configuration)[0].language
    return when (lang) {
        getString(R.string.arabic_code) -> {
            EnumLanguage.ARABIC
        }
        else -> {
            EnumLanguage.ENGLISH
        }
    }
}

//    inline operator fun <T> List<T>.component6(): T = get(5)

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine9(
    flow: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flow<R> = combine(
    combine(flow, flow2, flow3, ::Triple),
    combine(flow4, flow5, flow6, ::Triple),
    combine(flow7, flow8, flow9, ::Triple)
) { t1, t2, t3 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
        t3.first,
        t3.second,
        t3.third
    )
}

fun showReviewAppDialog(activity: Activity, onComplete: () -> Unit = {}) {
    Timber.i("TestReview showReviewAppDialog()")
    val manager = ReviewManagerFactory.create(activity)
    val request = manager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Timber.i("TestReview showReviewAppDialog() success")
            // We got the ReviewInfo object
            val reviewInfo = task.result
            val flow = manager.launchReviewFlow(activity, reviewInfo)
            flow.addOnCompleteListener { _ ->
                Timber.i("TestReview showReviewAppDialog() Complete")
                onComplete()
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }
        } else {
            Timber.i("TestReview showReviewAppDialog() not success")

            onComplete()
            // There was some problem, log or handle the error code.
        }
    }
}

