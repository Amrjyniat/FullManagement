package com.productivity.fullmangement

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.productivity.fullmangement.data.local.database.FullManagementRoomDataBase

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB = "tasks"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FullManagementRoomDataBase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // db has schema version 1. insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.

            insert("tasks", SQLiteDatabase.CONFLICT_FAIL, ContentValues().apply {
                put("title", "Powell")
                put("taskState", "high")
                put("repetition", "daily")
                put("priority", "high")
                put("isArchived", 0)
                put("isArchivedAfterCompleted", 0)
            })
            // Prepare for the next version.
            close()
        }


//        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    }
}