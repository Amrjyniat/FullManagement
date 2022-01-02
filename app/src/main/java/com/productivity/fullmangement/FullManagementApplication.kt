package com.productivity.fullmangement

import android.app.Application
import com.google.firebase.FirebaseApp
import com.productivity.fullmangement.data.local.datastore.DataStoreManager
import com.productivity.fullmangement.data.local.datastore.SharedPrefKey
import com.productivity.fullmangement.utils.getCurrLanguage
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class FullManagementApplication: Application() {

    @Inject
    lateinit var dataStoreManager: DataStoreManager


    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }


//        instance = this
    }

   /* companion object{
        lateinit var instance: Application private set
        private lateinit var activityContext: Context private set


    }

    fun getContext(): Context {
        return instance
    }

    fun getActivityContext(): Context {
        return activityContext
    }

    fun setActivityContext(context: Context?) {
        context?.let {
            activityContext = context
        }
    }

    object Strings {
        fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
            return try {
                activityContext.getString(stringRes, *formatArgs)
            } catch (exc: Exception) {
                Timber.i("TestError getting string from app, err: ${exc.message}")
                instance.getString(stringRes, *formatArgs)
            }
        }
    }*/
}
