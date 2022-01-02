package com.productivity.fullmangement.di

import android.content.Context
import androidx.room.Room
import com.productivity.fullmangement.data.local.database.FullManagementRoomDataBase
import com.productivity.fullmangement.data.local.database.TasksDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule{
    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): FullManagementRoomDataBase{//, tasksDaoProvider: Provider<TasksDao>
        return Room.databaseBuilder(
            context,
            FullManagementRoomDataBase::class.java,
            "FullManagement.db"
        )
        /*.addCallback(object : RoomDatabase.Callback(){
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // moving to a new thread
                CoroutineScope(Dispatchers.IO).launch {
                    tasksDaoProvider.get().insertPriorities(PREPOPULATE_PRIORITIES_DATA)
                    tasksDaoProvider.get().insertTaskStates(TaskState.PREPOPULATE_TASK_STATES_DATA)
                    tasksDaoProvider.get().insertRepetitions(Repetition.PREPOPULATE_REPETITIONS_DATA)
                }
            }
        })*/
        .build()
    }

    @Provides
    fun provideTasksDao(database: FullManagementRoomDataBase): TasksDao{
        return database.TasksDao()
    }
}
