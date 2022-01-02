package com.productivity.fullmangement.data.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.productivity.fullmangement.R
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("full_management_data_store")

@Singleton
class LoginRepository @Inject constructor(
    @ApplicationContext val context: Context
) {


    //Check if this is the first time open the app for the user
    var prefs: SharedPreferences =
        context.getSharedPreferences("full_management_data_store", Context.MODE_PRIVATE)

    /*suspend fun setIsSeenOnBoarding1(isSeenOnBoarding: Boolean = true){
       context.dataStore.edit { preferences->
           preferences[SharedPrefKey.isSeenOnBoarding] = isSeenOnBoarding
       }
   }

    val ss = runBlocking { flow { emit( context.dataStore.data.first()) } }

      val isSeenOnBoardingState1: Flow<Boolean> = runBlocking {
           context.dataStore.data
               .catch { exception ->
                   if (exception is IOException) {
                       Timber.d("DataStore exception: ${exception.message.toString()}")
                       emit(emptyPreferences())
                   } else {
                       throw exception
                   }
               }
               .map { preference ->
                   preference[SharedPrefKey.isSeenOnBoarding] ?: false
               }
       }*/

    fun setIsSeenOnBoarding(isSeenOnBoarding: Boolean = true){
        val prefsEditor = prefs.edit()
        prefsEditor.putBoolean("is_seen_on_boarding", isSeenOnBoarding)
        prefsEditor.apply()
    }

    val isSeenOnBoardingState = flow{
        emit(prefs.getBoolean("is_seen_on_boarding", false))
    }




}