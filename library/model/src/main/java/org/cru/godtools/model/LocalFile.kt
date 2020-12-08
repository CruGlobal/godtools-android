package org.cru.godtools.model

import org.cru.godtools.base.FileManager

data class LocalFile(val filename: String) {
    fun getFile(fileManager: FileManager) = fileManager.getFile(filename)
}
