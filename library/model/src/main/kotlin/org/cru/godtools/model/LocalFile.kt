package org.cru.godtools.model

import org.cru.godtools.base.FileManager

data class LocalFile(val filename: String) {
    suspend fun getFile(fileManager: FileManager) = fileManager.getFile(filename)
}
