package org.cru.godtools.articles.aem.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.room.converter.DateConverter;
import org.ccci.gto.android.common.room.converter.LocaleConverter;
import org.ccci.gto.android.common.room.converter.UriConverter;
import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
import org.cru.godtools.articles.aem.model.TranslationRef;

/**
 * This class is used to create the database table.
 *
 * @author Gyasi Story
 */
@Database(entities = {
        TranslationRef.class, TranslationRef.TranslationAemImport.class,
        AemImport.class, AemImport.AemImportArticle.class,
        Article.class, Attachment.class,
        ManifestAssociation.class
}, version = 1)
@TypeConverters({DateConverter.class, LocaleConverter.class, UriConverter.class})
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

    abstract TranslationDao translationDao();

    public abstract AemImportDao aemImportDao();

    public abstract ArticleDao articleDao();

    abstract ManifestAssociationDao manifestAssociationDao();

    abstract AttachmentDao attachmentDao();

    // endregion DAOs

    // region Repositories

    @NonNull
    public abstract TranslationRepository translationRepository();

    // endregion Repositories
}
