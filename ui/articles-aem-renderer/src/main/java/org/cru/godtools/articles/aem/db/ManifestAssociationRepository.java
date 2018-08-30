package org.cru.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;

import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.ManifestAssociation;

import java.util.List;


/**
 * This class is used a connector for the Data Access Object for Manifest Associations
 */
public class ManifestAssociationRepository {

    private final ManifestAssociationDao mManifestAssociationDao;

    public ManifestAssociationRepository(Context context) {

        ArticleRoomDatabase db = ArticleRoomDatabase.getInstance(context);
        mManifestAssociationDao = db.manifestAssociationDao();
    }

    /**
     * Inserts a new Association.  Conflicts will result in a replacement of conflicting
     * Association.
     * @param manifestAssociation = Association to be inserted.
     */
    public void insertAssociation(final ManifestAssociation manifestAssociation) {
        AsyncTask.execute(() -> mManifestAssociationDao.insertAssociation(manifestAssociation));
    }

    /**
     * Deletes a collection of Associations.
     *
     * @param manifestAssociations = Collection or single Association to be Deleted.
     */
    public void deleteAssociation(final ManifestAssociation... manifestAssociations) {
        AsyncTask.execute(() -> mManifestAssociationDao.deleteAssociations(manifestAssociations));
    }

    /**
     * Fetches a list of Associations based on the manifest ID
     * @param manifestID = The ID of the associated manifest.
     * @return = Live Data Collection of Associations.
     */
    public LiveData<List<ManifestAssociation>> getAssociationByManifestID(String manifestID) {
        return mManifestAssociationDao.getAssociationByManifestID(manifestID);
    }

    /**
     *  Fetches a list of Articles Associated to a Manifest.
     *
     * @param manifestID = the ID of the Manifest to query.
     * @return = Live Data Collection of Articles.
     */
    public LiveData<List<Article>> getArticlesByManifestID(String manifestID) {
        return mManifestAssociationDao.getArticlesByManifestID(manifestID);
    }


    //endregion
}
