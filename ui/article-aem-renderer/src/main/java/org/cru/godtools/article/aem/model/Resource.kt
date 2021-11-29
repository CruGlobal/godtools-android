package org.cru.godtools.article.aem.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import okhttp3.MediaType
import org.cru.godtools.base.FileSystem

@Entity(tableName = Resource.TABLE_NAME)
class Resource(@field:PrimaryKey val uri: Uri) {
    companion object {
        internal const val TABLE_NAME = "resources"
    }

    var contentType: MediaType? = null
    var localFileName: String? = null
    var dateDownloaded: Date? = null

    val isDownloaded: Boolean
        get() = localFileName != null

    /**
     * @return true if this resource needs to be downloaded
     */
    fun needsDownload(): Boolean {
        return localFileName == null || dateDownloaded == null
    }

    suspend fun getLocalFile(fs: FileSystem) = localFileName?.let { fs.getFile(it) }
    suspend fun getInputStream(fs: FileSystem) = getLocalFile(fs)?.inputStream()
}
