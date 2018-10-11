package org.cru.godtools.articles.aem.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.net.Uri
import android.support.annotation.WorkerThread
import org.cru.godtools.articles.aem.model.Resource
import java.util.*

@Dao
interface ResourceDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(resource: Resource)

    @WorkerThread
    @Query("UPDATE resources SET localFileName = :fileName, dateDownloaded = :downloadDate WHERE uri = :uri")
    fun updateLocalFile(uri: Uri, fileName: String?, downloadDate: Date?)

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

    @Query("""
        SELECT r.*
        FROM resources AS r JOIN articleResources AS a ON a.resourceUri = r.uri
        WHERE a.articleUri = :uri
        """)
    fun getAllLiveForArticle(uri: Uri): LiveData<List<Resource>>
}
