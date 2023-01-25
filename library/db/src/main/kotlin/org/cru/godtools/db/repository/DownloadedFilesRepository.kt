package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.DownloadedTranslationFile

interface DownloadedFilesRepository {
    suspend fun findDownloadedFile(filename: String): DownloadedFile?
    suspend fun getDownloadedFiles(): Collection<DownloadedFile>
    suspend fun getDownloadedTranslationFiles(): List<DownloadedTranslationFile>
    fun getDownloadedFilesFlow(): Flow<Collection<DownloadedFile>>

    fun insertOrIgnore(file: DownloadedFile)
    fun insertOrIgnore(translationFile: DownloadedTranslationFile)
    fun delete(file: DownloadedFile)
    suspend fun delete(file: DownloadedTranslationFile)
}
