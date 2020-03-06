package org.cru.godtools.article.aem.model

import android.content.Context
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import okhttp3.MediaType
import org.cru.godtools.article.aem.util.getFile
import java.io.File
import java.io.FileInputStream
import java.util.Date

@Entity(tableName = TABLE_NAME_RESOURCE)
class Resource(@field:PrimaryKey val uri: Uri) {
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

    fun getInputStream(context: Context) = getLocalFile(context)?.let { FileInputStream(it) }
}
