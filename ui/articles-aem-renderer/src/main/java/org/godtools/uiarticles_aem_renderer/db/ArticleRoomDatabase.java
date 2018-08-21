package org.godtools.uiarticles_aem_renderer.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import org.godtools.uiarticles_aem_renderer.model.Article;
import org.godtools.uiarticles_aem_renderer.model.Attachment;
import org.godtools.uiarticles_aem_renderer.model.ManifestAssociation;

/**
 *
 */
@Database(entities = {Article.class, ManifestAssociation.class, Attachment.class}, version = 1)
public abstract class ArticleRoomDatabase extends RoomDatabase {

    public abstract ArticleDao mArticleDao();
    public abstract ManifestAssociationDao mManifestAssociationDao();
    public abstract AttachmentDao mAttachmentDao();

    private static ArticleRoomDatabase INSTANCE;

    /**
     *
     * @param _context
     * @return
     */
    static ArticleRoomDatabase getINSTANCE(final Context _context){
        if (INSTANCE == null){
            synchronized (ArticleRoomDatabase.class){
                if (INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(_context.getApplicationContext(),
                            ArticleRoomDatabase.class, "article_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
