package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool

interface AttachmentsRepository {
    suspend fun findAttachment(id: Long): Attachment?
    fun findAttachmentFlow(id: Long): Flow<Attachment?>
    suspend fun getAttachments(): List<Attachment>
    fun getAttachmentsFlow(): Flow<List<Attachment>>

    suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean)

    // region Initial Content Methods
    fun storeInitialAttachments(attachments: Collection<Attachment>)
    // endregion Initial Content Methods

    // region Sync Methods
    fun storeAttachmentsFromSync(attachments: Collection<Attachment>)
    fun removeAttachmentsMissingFromSync(toolId: Long, syncedAttachments: Collection<Attachment>)
    fun deleteAttachmentsFor(tool: Tool)
    // endregion Sync Methods
}
