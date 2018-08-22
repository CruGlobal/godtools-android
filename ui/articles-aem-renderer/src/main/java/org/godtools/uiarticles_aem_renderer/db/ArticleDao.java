package org.godtools.uiarticles_aem_renderer.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import org.godtools.uiarticles_aem_renderer.model.Article;

import java.util.List;

/**
 *
 */
@Dao
public interface ArticleDao {
    /**
     *
     * @param article
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertArticle(Article article);

    /**
     *
     * @param articles
     */
    @Delete
    void deleteArticles(Article... articles);

    //TODO: Convert to LiveData after Testing
    /**
     *
     * @return
     */
    @Query("SELECT * FROM article_table")
    List<Article> getAllArticles();
}
