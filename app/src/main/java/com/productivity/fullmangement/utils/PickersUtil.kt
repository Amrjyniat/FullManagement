package com.productivity.fullmangement.utils

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import timber.log.Timber
import java.util.*

fun showDatePicker(
    activity: AppCompatActivity?,
    hasConstraintOnPastDays: Boolean = true,
    onSelectedDate: (Long) -> Unit
) {
    val picker = MaterialDatePicker.Builder.datePicker().apply {
        if (hasConstraintOnPastDays) {
            val calendar: Calendar = Calendar.getInstance()
            val calenderFrom = Calendar.getInstance()
            calenderFrom.set(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            val dateValidatorEnd = DateValidatorPointForward.from(calenderFrom.timeInMillis)
            val constraint = CalendarConstraints.Builder()
            constraint.setValidator(dateValidatorEnd)
            setCalendarConstraints(constraint.build())
        }
    }.build()
    activity?.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            onSelectedDate(it)
        }
    }
}
fun showTimePicker(activity: AppCompatActivity?, onSelectedTime: (MaterialTimePicker) -> Unit) {
    val picker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).build()
    activity?.let {
        picker.show(it.supportFragmentManager, picker.toString())
        picker.addOnPositiveButtonClickListener {
            onSelectedTime(picker)
        }
    }
}