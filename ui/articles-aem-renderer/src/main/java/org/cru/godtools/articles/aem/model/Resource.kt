package org.cru.godtools.articles.aem.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import org.cru.godtools.articles.aem.util.getFile
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Date

@Entity(tableName = "resources")
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

    @Throws(IOException::class)
    fun getInputStream(context: Context): InputStream? {
        return getLocalFile(context)?.run {
            if (isFile) {
                FileInputStream(this)
            } else {
                null
            }
        }
    }
}
