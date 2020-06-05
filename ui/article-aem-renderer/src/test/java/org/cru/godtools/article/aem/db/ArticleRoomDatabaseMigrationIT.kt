package org.cru.godtools.article.aem.db

import androidx.core.database.getStringOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [19])
class ArticleRoomDatabaseMigrationIT {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(), ArticleRoomDatabase::class.java.canonicalName
    )

    @Test
    fun migrate8To9() {
        // create v8 database
        helper.createDatabase(ArticleRoomDatabase.DATABASE_NAME, 8)
            .close()

        // run migration
        helper.runMigrationsAndValidate(ArticleRoomDatabase.DATABASE_NAME, 9, true, MIGRATION_8_9)
    }

    @Test
    fun migrate9To10() {
        // create v9 database
        helper.createDatabase(ArticleRoomDatabase.DATABASE_NAME, 9)
            .close()

        // run migration
        helper.runMigrationsAndValidate(ArticleRoomDatabase.DATABASE_NAME, 10, true, MIGRATION_9_10)
    }

    @Test
    fun migrate10To11() {
        // create v10 database
        helper.createDatabase(ArticleRoomDatabase.DATABASE_NAME, 10).apply {
            execSQL(
                """
                INSERT INTO articles (uri, uuid, title, canonicalUri, shareUri, date_created, date_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?)""",
                arrayOf("uri:1", "", "", "https://www.example.com", "https://www.example.com", 0, 0)
            )
            close()
        }

        // run migration & validate
        helper.runMigrationsAndValidate(ArticleRoomDatabase.DATABASE_NAME, 11, true, MIGRATION_10_11)
            .apply {
                query("SELECT canonicalUri, shareUri FROM articles").apply {
                    assertEquals(1, count)
                    moveToFirst()
                    assertNull(getStringOrNull(0))
                    assertNull(getStringOrNull(1))
                    close()
                }
            }
    }
}
