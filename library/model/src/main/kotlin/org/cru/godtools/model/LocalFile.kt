package org.cru.godtools.model

import org.cru.godtools.base.FileSystem

data class LocalFile(val filename: String) {
    suspend fun getFile(fs: FileSystem) = fs.file(filename)
}
