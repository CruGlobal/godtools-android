package org.keynote.godtools.android.db.repository

import android.database.sqlite.SQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.model.DownloadedFile
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyDownloadedFilesRepository @Inject constructor(private val dao: GodToolsDao) :
    DownloadedFilesRepository {
    override suspend fun findDownloadedFile(filename: String): DownloadedFile? = dao.find(filename)
    override suspend fun getDownloadedFiles() = dao.getAsync(Query.select<DownloadedFile>()).await()
    override fun getDownloadedFilesFlow() = dao.getAsFlow(Query.select<DownloadedFile>())

    override fun insertOrIgnore(file: DownloadedFile) {
        dao.insert(file, SQLiteDatabase.CONFLICT_IGNORE)
    }

    override fun delete(file: DownloadedFile) {
        dao.delete(file)
    }
}
