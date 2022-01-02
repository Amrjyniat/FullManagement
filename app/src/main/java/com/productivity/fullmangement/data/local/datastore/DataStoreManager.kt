package com.productivity.fullmangement.data.local.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("full_management_data_store")

    private fun <T> DataStore<Preferences>.getFromLocalStorage(
        PreferencesKey: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        return data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            val value = it[PreferencesKey]
            Timber.i("TestValue value: $value")
            if (value == null){
                storeValue(PreferencesKey, defaultValue)
            }
            it[PreferencesKey] ?: defaultValue
        }
    }

    suspend fun <T> storeValue(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit {
            it[key] = value
        }
    }

    fun <T> readValue(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return context.dataStore.getFromLocalStorage(key, defaultValue)
    }

    var prefs: SharedPreferences =
        context.getSharedPreferences("full_management_data_store", Context.MODE_PRIVATE)

    fun setIsSeenOnBoarding(isSeenOnBoarding: Boolean = true){
        val prefsEditor = prefs.edit()
        prefsEditor.putBoolean("is_seen_on_boarding", isSeenOnBoarding)
        prefsEditor.apply()
    }

    val isSeenOnBoardingState = flow{
        emit(prefs.getBoolean("is_seen_on_boarding", false))
    }
}