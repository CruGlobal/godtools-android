package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.DownloadedFile

interface DownloadedFilesRepository {
    suspend fun findDownloadedFile(filename: String): DownloadedFile?
    suspend fun getDownloadedFiles(): Collection<DownloadedFile>
    fun getDownloadedFilesFlow(): Flow<Collection<DownloadedFile>>

    fun insertOrIgnore(file: DownloadedFile)
    fun delete(file: DownloadedFile)
}
