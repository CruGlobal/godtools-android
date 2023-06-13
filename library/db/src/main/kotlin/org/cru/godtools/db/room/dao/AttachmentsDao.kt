package org.cru.godtools.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.cru.godtools.db.room.entity.AttachmentEntity
import org.cru.godtools.db.room.entity.partial.SyncAttachment

@Dao
internal interface AttachmentsDao {
    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun findAttachment(id: Long): AttachmentEntity?
    @Query("SELECT * FROM attachments WHERE id = :id")
    fun findAttachmentFlow(id: Long): Flow<AttachmentEntity?>

    @Query("SELECT * FROM attachments")
    suspend fun getAttachments(): List<AttachmentEntity>
    @Query("SELECT * FROM attachments")
    fun getAttachmentsFlow(): Flow<List<AttachmentEntity>>
    @Query("SELECT * FROM attachments WHERE tool = :toolCode")
    fun getAttachmentsForTool(toolCode: String): List<AttachmentEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrIgnore(attachment: AttachmentEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAttachments(attachments: Collection<AttachmentEntity>)
    @Upsert(entity = AttachmentEntity::class)
    fun upsertSyncAttachments(attachments: Collection<SyncAttachment>)
    @Query("UPDATE attachments SET isDownloaded = :isDownloaded WHERE id = :id")
    suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean)

    @Delete
    fun delete(attachment: AttachmentEntity)
    @Query("DELETE FROM attachments WHERE tool = :toolCode")
    fun deleteAttachmentsForTool(toolCode: String)
}
