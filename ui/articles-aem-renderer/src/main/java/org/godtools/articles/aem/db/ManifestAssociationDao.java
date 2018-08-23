package org.godtools.articles.aem.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.godtools.articles.aem.model.Article;
import org.godtools.articles.aem.model.ManifestAssociation;

import java.util.List;

/**
 * The Data Access Object fo the Manifest Association.
 */
@Dao
interface ManifestAssociationDao {
    /**
     * To insert a new ManifestAssociation.  If any conflict arrives, the
     * data will be replaced with the latest insert.
     *
     * @param manifestAssociation = the association to be saved.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAssociation(ManifestAssociation manifestAssociation);

    /**
     * This method will delete a collection of ManifestAssociations.
     *
     * @param manifestAssociations = the collection to be deleted.
     */
    @Delete
    void deleteAssociations(ManifestAssociation... manifestAssociations);

    /**
     *
     * @param manifestID
     * @return
     */
    @Query("SELECT * FROM manifest_association_table WHERE manifest_key = :manifestID")
    LiveData<List<ManifestAssociation>> getAssociationByManifestID(String manifestID);

    //todo: Convert to LiveData after Testing
    /**
     *
     * @param manifestID
     * @return
     */
    @Query("SELECT * FROM article_table AS art " +
            "INNER JOIN manifest_association_table AS assc ON art._id == assc.article_key " +
            "WHERE assc.manifest_key = :manifestID")
    LiveData<List<Article>> getArticlesByManifestID(String manifestID);

}
