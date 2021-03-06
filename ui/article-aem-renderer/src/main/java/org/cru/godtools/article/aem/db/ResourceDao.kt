package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date
import okhttp3.MediaType
import org.cru.godtools.article.aem.model.Resource

@Dao
interface ResourceDao {
    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(resource: Resource)

    @WorkerThread
    @Query(
        """
        UPDATE resources
        SET contentType = :contentType, localFileName = :fileName, dateDownloaded = :downloadDate
        WHERE uri = :uri
        """
    )
    fun updateLocalFile(uri: Uri, contentType: MediaType?, fileName: String?, downloadDate: Date?)

    @WorkerThread
    @Query(
        """
        DELETE FROM resources
        WHERE uri NOT IN (SELECT resourceUri FROM articleResources)
        """
    )
    fun removeOrphanedResources()

    @AnyThread
    @Query("SELECT * FROM resources WHERE uri = :uri")
    suspend fun find(uri: Uri): Resource?

    @WorkerThread
    @Query("SELECT * FROM resources")
    fun getAll(): List<Resource>

    @AnyThread
    @Query(
        """
        SELECT r.*
        FROM resources AS r JOIN articleResources AS a ON a.resourceUri = r.uri
        WHERE a.articleUri = :uri
        """
    )
    suspend fun getAllForArticle(uri: Uri): List<Resource>
}
