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
        helper.createDatabase(DATABASE_NAME, 8)
                .close()

        // run migration
        helper.runMigrationsAndValidate(DATABASE_NAME, 9, true, MIGRATION_8_9)
    }

    @Test
    fun migrate9To10() {
        // create v9 database
        helper.createDatabase(DATABASE_NAME, 9)
            .close()

        // run migration
        helper.runMigrationsAndValidate(DATABASE_NAME, 10, true, MIGRATION_9_10)
    }
}
