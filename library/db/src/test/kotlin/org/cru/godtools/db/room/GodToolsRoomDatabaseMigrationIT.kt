package org.cru.godtools.db.room

import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GodToolsRoomDatabaseMigrationIT {
    companion object {
        private val MIGRATIONS = emptyArray<Migration>()
    }

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GodToolsRoomDatabase::class.java)

    @Test
    fun testMigrate1To2() {
        // create v1 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 1).apply {
            execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            close()
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 2, true, *MIGRATIONS)
            .apply {
                query("SELECT id, time FROM last_sync_times")
                    .apply {
                        assertEquals(1, count)
                        moveToFirst()
                        assertEquals("sync_time", getStringOrNull(0))
                        assertEquals(1234, getIntOrNull(1))
                    }
                    .close()
            }
    }

    @Test
    fun testMigrate2To3() {
        // create v2 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 2).apply {
            execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            close()
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 3, true, *MIGRATIONS)
            .apply {
                query("SELECT id, time FROM last_sync_times")
                    .apply {
                        assertEquals(1, count)
                        moveToFirst()
                        assertEquals("sync_time", getStringOrNull(0))
                        assertEquals(1234, getIntOrNull(1))
                    }
                    .close()
            }
    }

    @Test
    fun testMigrate3To4() {
        // create v3 database
        with(helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 3)) {
            execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            close()
        }

        // run migration
        with(helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 4, true, *MIGRATIONS)) {
            with(query("SELECT id, time FROM last_sync_times")) {
                assertEquals(1, count)
                moveToFirst()
                assertEquals("sync_time", getStringOrNull(0))
                assertEquals(1234, getIntOrNull(1))
                close()
            }
            close()
        }
    }
}
