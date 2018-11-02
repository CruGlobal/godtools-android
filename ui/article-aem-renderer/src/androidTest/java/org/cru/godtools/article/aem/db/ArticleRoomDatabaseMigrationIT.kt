package org.cru.godtools.article.aem.db

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test

class ArticleRoomDatabaseMigrationIT {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
            ArticleRoomDatabase::class.java.canonicalName)

    @Test
    fun migrate8To9() {
        // create v8 database
        helper.createDatabase(ArticleRoomDatabase.DATABASE_NAME, 8)
                .close()

        // run migration
        helper.runMigrationsAndValidate(ArticleRoomDatabase.DATABASE_NAME, 9, true, ArticleRoomDatabase.MIGRATION_8_9)
    }
}
