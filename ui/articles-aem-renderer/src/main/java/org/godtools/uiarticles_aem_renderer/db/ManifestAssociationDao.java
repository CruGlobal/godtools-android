package org.godtools.uiarticles_aem_renderer.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.godtools.uiarticles_aem_renderer.model.Article;
import org.godtools.uiarticles_aem_renderer.model.ManifestAssociation;

import java.util.List;

/**
 *
 */
@Dao
public interface ManifestAssociationDao {
    /**
     *
     * @param manifestAssociation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAssocation(ManifestAssociation manifestAssociation);

    /**
     *
     * @param manifestAssociations
     */
    @Delete
    void deleteAssociations(ManifestAssociation... manifestAssociations);

    /**
     *
     * @param manifestID
     * @return
     */
    @Query("SELECT * FROM manifest_association_table WHERE manifest_key = :manifestID")
    List<ManifestAssociation> getAssociationByManifestID(String manifestID);

    //TODO: Convert to LiveData after Testing
    /**
     *
     * @param manifestID
     * @return
     */
    @Query("SELECT * FROM article_table AS art " +
            "INNER JOIN manifest_association_table AS assc ON art._id == assc.article_key " +
            "WHERE assc.manifest_key = :manifestID")
    List<Article> getArticlesByManifestID(String manifestID);

}
