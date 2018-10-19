package org.cru.godtools.articles.aem.db

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.net.Uri
import android.support.annotation.WorkerThread
import okhttp3.MediaType
import org.cru.godtools.articles.aem.model.Resource
import java.util.Date

@Dao
interface ResourceDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(resource: Resource)

    @WorkerThread
    @Query("""
        UPDATE resources
        SET contentType = :contentType, localFileName = :fileName, dateDownloaded = :downloadDate
        WHERE uri = :uri""")
    fun updateLocalFile(uri: Uri, contentType: MediaType?, fileName: String?, downloadDate: Date?)

    @WorkerThread
    @Query("SELECT * FROM resources WHERE uri = :uri")
    fun find(uri: Uri): Resource?

    @Query("SELECT * FROM resources")
    fun getAll(): List<Resource>

    @Query("""
        SELECT r.*
        FROM resources AS r JOIN articleResources AS a ON a.resourceUri = r.uri
        WHERE a.articleUri = :uri
        """)
    fun getAllForArticle(uri: Uri): List<Resource>
}
