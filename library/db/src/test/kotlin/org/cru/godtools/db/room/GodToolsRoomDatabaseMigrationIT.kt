package org.cru.godtools.db.room

import android.database.SQLException
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.ccci.gto.android.common.util.database.getString
import org.ccci.gto.android.common.util.database.map
import org.cru.godtools.model.Tool
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GodToolsRoomDatabaseMigrationIT {
    companion object {
        private val MIGRATIONS = emptyArray<Migration>()
    }

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), GodToolsRoomDatabase::class.java)

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
            db.query(filesQuery).close()
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
            db.query(toolsQuery).close()
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
            db.query(attachmentsQuery).close()
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
            db.query(translationsQuery).close()
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

    @Test
    fun testMigrate14To15() {
        // create v14 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 14).use { db ->
            db.execSQL("""INSERT INTO tools (id, code, type) VALUES (1, "a", "TRACT")""")
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 15, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, code, changedFields FROM tools").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals("a", it.getStringOrNull(1))
                assertEquals("", it.getStringOrNull(2))
            }
        }
    }

    @Test
    fun testMigrate15To16() {
        val name = UUID.randomUUID().toString()

        // create v15 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 15).use { db ->
            db.execSQL("INSERT INTO users (id, name) VALUES (1, ?)", arrayOf(name))
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("last_synced.user:1", "1234"))
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 16, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, name, givenName, familyName, email FROM users").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals(name, it.getStringOrNull(1))
                assertNull(it.getStringOrNull(2))
                assertNull(it.getStringOrNull(3))
                assertNull(it.getStringOrNull(4))
            }

            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
        }
    }

    @Test
    fun testMigrate16To17() {
        // create v16 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 16).use { db ->
            db.execSQL("""INSERT INTO users (id) VALUES (1)""")
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 17, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, isInitialFavoriteToolsSynced FROM users").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals(0, it.getIntOrNull(1))
            }
        }
    }

    @Test
    fun testMigrate17To18() {
        // create v17 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 17).use { db ->
            db.execSQL("""INSERT INTO languages (id, code) VALUES (1, "en")""")
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 18, true, *MIGRATIONS).use { db ->
            db.query("SELECT apiId, code FROM languages").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals("en", it.getStringOrNull(1))
            }
        }
    }

    @Test
    fun testMigrate18To19() {
        // create v18 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 18).use { db ->
            db.execSQL("""INSERT INTO tools (id, code, type) VALUES (1, "a", "TRACT")""")
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 19, true, *MIGRATIONS).use { db ->
            db.query("SELECT apiId, code, type FROM tools").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals(1, it.getIntOrNull(0))
                assertEquals("a", it.getStringOrNull(1))
                assertEquals("TRACT", it.getStringOrNull(2))
            }
        }
    }

    @Test
    fun testMigrate19To20() {
        val filesQuery = "SELECT * FROM downloadedTranslationFiles"

        // create v19 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 19).use { db ->
            db.execSQL("INSERT INTO last_sync_times (id, time) VALUES (?, ?)", arrayOf("sync_time", "1234"))
            assertFailsWith<SQLException> { db.query(filesQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 20, true, *MIGRATIONS).use { db ->
            db.query("SELECT id, time FROM last_sync_times").use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("sync_time", it.getStringOrNull(0))
                assertEquals(1234, it.getIntOrNull(1))
            }
            db.query(filesQuery).close()
        }
    }

    @Test
    fun testMigrate20To21() {
        val defaultLocaleQuery = "SELECT code, defaultLocale FROM tools WHERE code = 'kgp'"

        // create v20 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 20).use { db ->
            db.execSQL("INSERT INTO tools (code, type) VALUES (?, ?)", arrayOf("kgp", Tool.Type.TRACT))
            assertFailsWith<SQLException> { db.query(defaultLocaleQuery) }
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 21, true, *MIGRATIONS).use { db ->
            db.query(defaultLocaleQuery).use {
                assertEquals(1, it.count)
                it.moveToFirst()
                assertEquals("kgp", it.getStringOrNull(0))
                assertEquals("en", it.getStringOrNull(1))
            }
        }
    }

    @Test
    fun testMigrate21To22() {
        // create v21 database
        helper.createDatabase(GodToolsRoomDatabase.DATABASE_NAME, 21).use { db ->
            assertFalse(db.dumpIndices("translations").values.any { it == setOf("locale") })
        }

        // run migration
        helper.runMigrationsAndValidate(GodToolsRoomDatabase.DATABASE_NAME, 22, true, *MIGRATIONS).use { db ->
            assertTrue(db.dumpIndices("translations").values.any { it == setOf("locale") })
        }
    }

    private fun SupportSQLiteDatabase.dumpIndices(table: String) = query("PRAGMA index_list($table)").use { it ->
        it.map { it.getString(1) }.associateWith { name ->
            query("PRAGMA index_info($name)").use { it.map { it.getString(2) }.toSet() }
        }
    }
}
