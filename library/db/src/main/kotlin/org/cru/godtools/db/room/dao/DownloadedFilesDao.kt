package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.DownloadedFileEntity

@Dao
internal interface DownloadedFilesDao {
    @Query("SELECT * FROM downloadedFiles WHERE filename = :filename")
    suspend fun findDownloadedFile(filename: String): DownloadedFileEntity?
    @Query("SELECT * FROM downloadedFiles")
    suspend fun getDownloadedFiles(): List<DownloadedFileEntity>
    @Query("SELECT * FROM downloadedFiles")
    fun getDownloadedFilesFlow(): Flow<List<DownloadedFileEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(file: DownloadedFileEntity)

    @Delete
    fun delete(file: DownloadedFileEntity)
}
