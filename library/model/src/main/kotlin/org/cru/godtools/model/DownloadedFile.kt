package org.cru.godtools.model

import org.cru.godtools.base.FileSystem

@JvmInline
value class DownloadedFile(val filename: String) {
    suspend fun getFile(fs: FileSystem) = fs.file(filename)
}

@Deprecated(
    "This class was renamed to DownloadedFile",
    ReplaceWith("DownloadedFile", "org.cru.godtools.model.DownloadedFile")
)
typealias LocalFile = DownloadedFile
