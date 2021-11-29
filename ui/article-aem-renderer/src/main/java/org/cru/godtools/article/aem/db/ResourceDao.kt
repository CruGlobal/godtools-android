package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date
import okhttp3.MediaType
import org.cru.godtools.article.aem.model.Resource

@Dao
internal interface ResourceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(resource: Resource)

    @Query(
        """
        UPDATE resources
        SET contentType = :contentType, localFileName = :fileName, dateDownloaded = :downloadDate
        WHERE uri = :uri
        """
    )
    suspend fun updateLocalFile(uri: Uri, contentType: MediaType?, fileName: String?, downloadDate: Date?)

    @Query(
        """
        DELETE FROM resources
        WHERE uri NOT IN (SELECT resourceUri FROM articleResources)
        """
    )
    suspend fun removeOrphanedResources()

    @Query("SELECT * FROM resources WHERE uri = :uri")
    suspend fun find(uri: Uri): Resource?
    @Query("SELECT * FROM resources")
    suspend fun getAll(): List<Resource>
    @Query(
        """
        SELECT r.*
        FROM resources AS r JOIN articleResources AS a ON a.resourceUri = r.uri
        WHERE a.articleUri = :uri
        """
    )
    suspend fun getAllForArticle(uri: Uri): List<Resource>
}
