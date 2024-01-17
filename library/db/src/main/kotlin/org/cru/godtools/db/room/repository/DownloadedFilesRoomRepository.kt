package org.cru.godtools.db.room.repository

import androidx.room.Dao
import kotlinx.coroutines.flow.map
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.DownloadedFileEntity.Companion.toEntity
import org.cru.godtools.db.room.entity.DownloadedTranslationFileEntity.Companion.toEntity
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.DownloadedTranslationFile

@Dao
internal abstract class DownloadedFilesRoomRepository(private val db: GodToolsRoomDatabase) :
    DownloadedFilesRepository {
    private val dao get() = db.downloadedFilesDao

    override suspend fun findDownloadedFile(filename: String) = dao.findDownloadedFile(filename)?.toModel()

    override suspend fun getDownloadedFiles() = dao.getDownloadedFiles().map { it.toModel() }
    override suspend fun getDownloadedTranslationFiles() = dao.getDownloadedTranslationFiles().map { it.toModel() }
    override fun getDownloadedFilesFlow() = dao.getDownloadedFilesFlow().map { it.map { it.toModel() } }

    override suspend fun insertOrIgnore(file: DownloadedFile) = dao.insertOrIgnore(file.toEntity())
    override suspend fun insertOrIgnore(translationFile: DownloadedTranslationFile) =
        dao.insertOrIgnore(translationFile.toEntity())

    override fun delete(file: DownloadedFile) = dao.delete(file.toEntity())
    override suspend fun delete(file: DownloadedTranslationFile) = dao.delete(file.toEntity())
}
