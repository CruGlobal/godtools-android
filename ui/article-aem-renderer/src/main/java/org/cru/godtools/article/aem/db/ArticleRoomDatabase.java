package org.cru.godtools.article.aem.db;

import android.content.Context;

import org.ccci.gto.android.common.room.converter.DateConverter;
import org.ccci.gto.android.common.room.converter.LocaleConverter;
import org.ccci.gto.android.common.room.converter.UriConverter;
import org.cru.godtools.article.aem.model.AemImport;
import org.cru.godtools.article.aem.model.Article;
import org.cru.godtools.article.aem.model.Resource;
import org.cru.godtools.article.aem.model.TranslationRef;
import org.cru.godtools.article.aem.room.converter.MediaTypeConverter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * This class is used to create the database table.
 *
 * @author Gyasi Story
 */
@Database(entities = {
        TranslationRef.class, TranslationRef.TranslationAemImport.class,
        AemImport.class, AemImport.AemImportArticle.class,
        Article.class, Article.Tag.class,
        Article.ArticleResource.class, Resource.class
}, version = 10)
@TypeConverters({DateConverter.class, LocaleConverter.class, MediaTypeConverter.class, UriConverter.class})
public abstract class ArticleRoomDatabase extends RoomDatabase {
    @VisibleForTesting
    static final String DATABASE_NAME = "aem_article_cache.db";

    /*
     * Version history
     *
     * v5.0.18
     * 9: 2018-10-30
     * 10: 2018-11-06
     */

    ArticleRoomDatabase() {}

    @Nullable
    private static ArticleRoomDatabase sInstance;

    /**
     * Used to get the current Instance of the Room database.
     *
     * @param context = the application context
     * @return = Instance of the current Room Database
     */
    public static synchronized ArticleRoomDatabase getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(), ArticleRoomDatabase.class, DATABASE_NAME)
                    .addMigrations(MIGRATION_8_9)
                    .addMigrations(MIGRATION_9_10)
                    .fallbackToDestructiveMigration()
                    .build();
        }

        return sInstance;
    }

    // region DAOs

    @NonNull
    abstract TranslationDao translationDao();

    @NonNull
    public abstract AemImportDao aemImportDao();

    @NonNull
    public abstract ArticleDao articleDao();

    @NonNull
    public abstract ResourceDao resourceDao();

    // endregion DAOs

    // region Repositories

    @NonNull
    public abstract TranslationRepository translationRepository();

    @NonNull
    public abstract AemImportRepository aemImportRepository();

    @NonNull
    public abstract ArticleRepository articleRepository();

    @NonNull
    public abstract ResourceRepository resourceRepository();

    // endregion Repositories

    // region Migrations

    @VisibleForTesting
    static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE articles ADD COLUMN `canonicalUri` TEXT");
            database.execSQL("ALTER TABLE articles ADD COLUMN `shareUri` TEXT");
        }
    };
    static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(@NonNull final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE aemImports ADD COLUMN `lastAccessed` INTEGER NOT NULL DEFAULT 0");
        }
    };

    // endregion Migrations
}
