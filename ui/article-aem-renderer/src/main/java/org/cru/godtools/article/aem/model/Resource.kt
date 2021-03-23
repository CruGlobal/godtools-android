package org.cru.godtools.article.aem.model

import android.content.Context
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.io.FileInputStream
import java.util.Date
import okhttp3.MediaType
import org.cru.godtools.article.aem.util.getFile
import org.cru.godtools.base.FileManager

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

    fun getLocalFile(context: Context): File? {
        return getFile(context, localFileName)
    }

    suspend fun getLocalFile(fileManager: FileManager) = localFileName?.let { fileManager.getFile(it) }

    fun getInputStream(context: Context) = getLocalFile(context)?.let { FileInputStream(it) }
}
