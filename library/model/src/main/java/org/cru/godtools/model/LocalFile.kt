package org.cru.godtools.model

import android.content.Context
import org.cru.godtools.base.FileManager
import org.cru.godtools.base.util.getGodToolsFile

data class LocalFile(val filename: String) {
    fun getFile(context: Context) = context.getGodToolsFile(filename)
    fun getFile(fileManager: FileManager) = fileManager.getFile(filename)
}
