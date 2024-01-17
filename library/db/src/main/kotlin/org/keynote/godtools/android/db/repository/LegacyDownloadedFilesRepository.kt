package org.keynote.godtools.android.db.repository

import android.database.sqlite.SQLiteDatabase
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.model.DownloadedTranslationFile
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyDownloadedFilesRepository @Inject constructor(
    private val dao: GodToolsDao,
    roomDb: GodToolsRoomDatabase,
) : DownloadedFilesRepository by roomDb.downloadedFilesRepository {
    override suspend fun getDownloadedTranslationFiles() =
        dao.getAsync(Query.select<DownloadedTranslationFile>()).await()

    override suspend fun insertOrIgnore(translationFile: DownloadedTranslationFile) {
        dao.insertAsync(translationFile, SQLiteDatabase.CONFLICT_IGNORE).join()
    }

    override suspend fun delete(file: DownloadedTranslationFile) = dao.deleteAsync(file).join()
}
