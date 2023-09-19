package org.cru.godtools.db.room

import android.database.SQLException
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import org.ccci.gto.android.common.util.database.getString
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

    @Test
    fun testMigrate4To5() {
        val followupsQuery = "SELECT * FROM followups"

        // create v4 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 4).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(followupsQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 5, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(followupsQuery)
        }
    }

    @Test
    fun testMigrate5To6() {
        val languagesQuery = "SELECT * FROM languages"

        // create v5 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 5).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(languagesQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 6, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(languagesQuery)
        }
    }

    @Test
    fun testMigrate6To7() {
        val guid = UUID.randomUUID().toString()
        val usersQuery = "SELECT * FROM users"

        // create v6 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 6).use { db ->
            db.execSQL("INSERT INTO users (id, ssoGuid) VALUES (?, ?)", arrayOf("user1", guid))
            db.query(usersQuery).use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("user1", it.getString("id"))
                assertEquals(guid, it.getString("ssoGuid"))
            }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 7, true, *MIGRATIONS).use { db ->
            db.query(usersQuery).use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("user1", it.getString("id"))
                assertEquals(guid, it.getString("ssoGuid"))
                assertNotEquals(-1, it.getColumnIndex("name"))
            }
        }
    }

    @Test
    fun testMigrate7To8() {
        val filesQuery = "SELECT * FROM downloadedFiles"

        // create v7 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 7).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(filesQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 8, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(filesQuery)
        }
    }

    @Test
    fun testMigrate8To9() {
        val toolsQuery = "SELECT * FROM tools"

        // create v8 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 8).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(toolsQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 9, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(toolsQuery)
        }
    }

    @Test
    fun testMigrate9To10() {
        val attachmentsQuery = "SELECT * FROM attachments"

        // create v9 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 9).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(attachmentsQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 10, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(attachmentsQuery)
        }
    }

    @Test
    fun testMigrate10To11() {
        val translationsQuery = "SELECT * FROM translations"

        // create v10 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 10).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(translationsQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 11, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(translationsQuery)
        }
    }

    @Test
    fun testMigrate11To12() {
        // create v11 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 11).use { db ->
            db.execSQL("INSERT INTO languages (id, code) VALUES (1, ?)", arrayOf("en"))
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 12, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, code, isAdded FROM languages").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals("en", it.getStringOrNull(1))
                assertEquals(0, it.getIntOrNull(2))
            }
        }
    }

    @Test
    fun testMigrate12To13() {
        // create v12 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 12).use {}

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 13, true, *MIGRATIONS).use {}
    }

    @Test
    fun testMigrate13To14() {
        // create v13 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 13).use { db ->
            db.execSQL("""INSERT INTO tools (id, code, type, isAdded) VALUES (1, "a", "TRACT", 1)""")
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 14, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, code, isFavorite FROM tools").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals("a", it.getStringOrNull(1))
                assertEquals(1, it.getIntOrNull(2))
            }
        }
    }
}
