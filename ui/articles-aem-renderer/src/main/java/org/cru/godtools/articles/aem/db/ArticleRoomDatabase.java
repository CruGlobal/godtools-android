package org.cru.godtools.articles.aem.db;

import android.content.Context;

import org.ccci.gto.android.common.room.converter.DateConverter;
import org.ccci.gto.android.common.room.converter.LocaleConverter;
import org.ccci.gto.android.common.room.converter.UriConverter;
import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Resource;
import org.cru.godtools.articles.aem.model.TranslationRef;
import org.cru.godtools.articles.aem.room.converter.MediaTypeConverter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

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
}, version = 8)
@TypeConverters({DateConverter.class, LocaleConverter.class, MediaTypeConverter.class, UriConverter.class})
public abstract class ArticleRoomDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "aem_article_cache.db";

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
}
