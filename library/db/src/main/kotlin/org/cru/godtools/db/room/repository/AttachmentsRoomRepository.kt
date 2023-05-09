package org.cru.godtools.db.room.repository

import androidx.room.Dao
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.room.changeFlow
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.room.GodToolsRoomDatabase
import org.cru.godtools.db.room.entity.AttachmentEntity
import org.cru.godtools.db.room.entity.partial.SyncAttachment
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool

@Dao
internal abstract class AttachmentsRoomRepository(private val db: GodToolsRoomDatabase) : AttachmentsRepository {
    private val dao get() = db.attachmentsDao

    override suspend fun findAttachment(id: Long) = dao.findAttachment(id)?.toModel()
    override fun findAttachmentFlow(id: Long) = dao.findAttachmentFlow(id).map { it?.toModel() }
    override suspend fun getAttachments() = dao.getAttachments().map { it.toModel() }
    override fun getAttachmentsFlow() = dao.getAttachmentsFlow().map { it.map { it.toModel() } }

    override fun attachmentsChangeFlow(): Flow<Any?> = db.changeFlow("attachments")

    override suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean) =
        dao.updateAttachmentDownloaded(id, isDownloaded)

    override suspend fun storeInitialAttachments(attachments: Collection<Attachment>) =
        dao.insertOrIgnoreAttachments(attachments.map { AttachmentEntity(it) })

    // region Sync Methods
    override fun storeAttachmentsFromSync(attachments: Collection<Attachment>) =
        dao.upsertSyncAttachments(attachments.map { SyncAttachment(it) })

    @Transaction
    override fun removeAttachmentsMissingFromSync(tool: Tool, syncedAttachments: Collection<Attachment>) {
        val toolCode = tool.code ?: return
        val keep = syncedAttachments.mapTo(mutableSetOf()) { it.id }
        dao.getAttachmentsForTool(toolCode)
            .filterNot { it.id in keep }
            .forEach { dao.delete(it) }
    }

    override fun deleteAttachmentsFor(tool: Tool) {
        tool.code?.let { dao.deleteAttachmentsForTool(it) }
    }
    // endregion Sync Methods
}
