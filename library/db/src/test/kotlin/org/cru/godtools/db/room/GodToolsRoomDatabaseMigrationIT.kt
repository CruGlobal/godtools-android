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

private val MIGRATIONS = emptyArray<Migration>()

@RunWith(AndroidJUnit4::class)
class GodToolsRoomDatabaseMigrationIT {
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
}
