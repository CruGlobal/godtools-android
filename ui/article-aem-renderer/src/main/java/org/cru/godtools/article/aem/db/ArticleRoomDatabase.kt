package org.cru.godtools.article.aem.db

import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.ccci.gto.android.common.androidx.room.converter.DateConverter
import org.ccci.gto.android.common.androidx.room.converter.LocaleConverter
import org.ccci.gto.android.common.androidx.room.converter.UriConverter
import org.cru.godtools.article.aem.model.AemImport
import org.cru.godtools.article.aem.model.Article
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.room.converter.MediaTypeConverter

@Database(
    entities = [
        TranslationRef::class, TranslationRef.TranslationAemImport::class, AemImport::class,
        AemImport.AemImportArticle::class, Article::class, Article.Tag::class, Article.ArticleResource::class,
        Resource::class
    ],
    version = 11
)
@TypeConverters(DateConverter::class, LocaleConverter::class, MediaTypeConverter::class, UriConverter::class)
abstract class ArticleRoomDatabase internal constructor() : RoomDatabase() {
    companion object {
        internal const val DATABASE_NAME = "aem_article_cache.db"
    }

    // region DAOs
    abstract fun aemImportDao(): AemImportDao
    abstract fun articleDao(): ArticleDao
    abstract fun resourceDao(): ResourceDao
    abstract fun translationDao(): TranslationDao
    // endregion DAOs

    // region Repositories
    abstract fun aemImportRepository(): AemImportRepository
    abstract fun articleRepository(): ArticleRepository
    abstract fun resourceRepository(): ResourceRepository
    abstract fun translationRepository(): TranslationRepository
    // endregion Repositories
}

// region Migrations
/*
 * Version history
 *
 * v5.0.18
 * 9: 2018-10-30
 * 10: 2018-11-06
 * 11: 2019-01-10
 * v5.1.1
 */

@VisibleForTesting
internal val MIGRATION_8_9: Migration = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE articles ADD COLUMN `canonicalUri` TEXT")
        database.execSQL("ALTER TABLE articles ADD COLUMN `shareUri` TEXT")
    }
}
@VisibleForTesting
internal val MIGRATION_9_10: Migration = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE aemImports ADD COLUMN `lastAccessed` INTEGER NOT NULL DEFAULT 0")
    }
}
@VisibleForTesting
internal val MIGRATION_10_11: Migration = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("UPDATE articles SET canonicalUri = null, shareUri = null")
    }
}

internal fun RoomDatabase.Builder<ArticleRoomDatabase>.enableMigrations() = addMigrations(MIGRATION_8_9)
    .addMigrations(MIGRATION_9_10)
    .addMigrations(MIGRATION_10_11)
    .fallbackToDestructiveMigration()
// endregion Migrations
