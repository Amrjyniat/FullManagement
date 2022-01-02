package com.productivity.fullmangement.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SharedPrefKey {
    val isSeenOnBoarding = booleanPreferencesKey("is_seen_on_boarding")

    val selectedAppLanguage = stringPreferencesKey("selected_app_language")

    val isRemindBeforeOneDay = booleanPreferencesKey("is_remind_before_one_day")
    val isRemindBeforeTwoHours = booleanPreferencesKey("is_remind_before_two_hours")
    val isRemindWhenDueDate = booleanPreferencesKey("is_remind_before_when_due_date")
}

object SharedPrefValue {
    const val DEFAULT_APP_LANGUAGE = "en"

    const val DEFAULT_IS_SEEN_ON_BOARDING = false

    const val DEFAULT_IS_REMIND_BEFORE_ONE_DAY = true
    const val DEFAULT_IS_REMIND_BEFORE_TWO_HOURS = true
    const val DEFAULT_IS_REMIND_WHEN_DUE_DATE = true
}