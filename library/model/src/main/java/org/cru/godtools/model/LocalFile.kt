package org.cru.godtools.model

import android.content.Context
import org.cru.godtools.base.util.getGodToolsFile

class LocalFile {
    companion object {
        @JvmField
        val INVALID_FILE_NAME: String? = null
    }

    var fileName: String? = null

    fun getFile(context: Context) = context.getGodToolsFile(fileName)
}
