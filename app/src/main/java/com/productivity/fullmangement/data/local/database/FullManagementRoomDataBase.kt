package com.productivity.fullmangement.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.productivity.fullmangement.utils.converters.Converters

@Database(entities = [TaskLocal::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class FullManagementRoomDataBase : RoomDatabase(){
    abstract fun TasksDao(): TasksDao
}

/*val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN test INTEGER NOT NULL default 0")
    }
}*/
