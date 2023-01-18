package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment

interface AttachmentsRepository {
    suspend fun findAttachment(id: Long): Attachment?
    fun findAttachmentFlow(id: Long): Flow<Attachment?>
    suspend fun getAttachments(): List<Attachment>
    fun getAttachmentsFlow(): Flow<List<Attachment>>

    suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean)

    // region Sync Methods
    fun storeAttachmentsFromSync(attachments: Collection<Attachment>)
    fun removeAttachmentsMissingFromSync(toolId: Long, syncedAttachments: Collection<Attachment>)
    // endregion Sync Methods
}
