package org.godtools.articles.aem.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import org.godtools.articles.aem.model.Article;
import org.godtools.articles.aem.model.ManifestAssociation;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 *
 */
public class ManifestAssociationReposistory {

    private  ManifestAssociationDao mManifestAssociationDao;

    /**
     *
     * @param application
     */
    public ManifestAssociationReposistory(Application application) {

        ArticleRoomDatabase db = ArticleRoomDatabase.getINSTANCE(application);
        mManifestAssociationDao = db.mManifestAssociationDao();
    }

    /**
     *
     * @param manifestAssociation
     */
    public void insertAssociation( final ManifestAssociation manifestAssociation){
        AsyncTask.execute(() -> mManifestAssociationDao.insertAssociation(manifestAssociation));
    }

    /**
     *
     * @param manifestAssociations
     */
    public void deleteAssociation(final ManifestAssociation... manifestAssociations){
        AsyncTask.execute(() -> mManifestAssociationDao.deleteAssociations(manifestAssociations));
    }

    //region getAssociationByManifestID

    /**
     *
     * @param manifestID
     * @return
     */
    public LiveData<List<ManifestAssociation>> getAssociationByManifestID(String manifestID) throws ExecutionException, InterruptedException {
        return new GetAssociationByManifestIDAsync(mManifestAssociationDao).execute(manifestID).get();
    }

    private static class GetAssociationByManifestIDAsync extends AsyncTask<String, Void, LiveData<List<ManifestAssociation>>>{
        private ManifestAssociationDao dao;
        public GetAssociationByManifestIDAsync(ManifestAssociationDao mManifestAssociationDao) {
            this.dao = mManifestAssociationDao;
        }

        @Override
        protected LiveData<List<ManifestAssociation>> doInBackground(String... strings) {
            return dao.getAssociationByManifestID(strings[0]);
        }
    }

    //endregion


    //region getArticlesByManifestID

    public LiveData<List<Article>> getArticlesByManifestID(String manifestID) throws ExecutionException, InterruptedException {
        return new ArticlesByManifestAsync(mManifestAssociationDao).execute(manifestID).get();
    }

    private static class ArticlesByManifestAsync extends AsyncTask<String, Void, LiveData<List<Article>>>{

        private ManifestAssociationDao doa;
        public ArticlesByManifestAsync(ManifestAssociationDao mManifestAssociationDao) {
            this.doa = mManifestAssociationDao;
        }

        @Override
        protected LiveData<List<Article>> doInBackground(String... strings) {
            return doa.getArticlesByManifestID(strings[0]);
        }
    }

    //endregion
}
