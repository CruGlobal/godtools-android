package org.cru.godtools.articles.aem.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.content.Context
import android.net.Uri
import org.cru.godtools.articles.aem.util.getFile
import java.io.File
import java.util.Date

@Entity(tableName = "resources")
class Resource(@field:PrimaryKey val uri: Uri) {
    var localFileName: String? = null
    var dateDownloaded: Date? = null

    /**
     * @return true if this resource needs to be downloaded
     */
    fun needsDownload(): Boolean {
        return localFileName == null || dateDownloaded == null
    }

    fun getLocalFile(context: Context): File? {
        return getFile(context, localFileName)
    }
}
