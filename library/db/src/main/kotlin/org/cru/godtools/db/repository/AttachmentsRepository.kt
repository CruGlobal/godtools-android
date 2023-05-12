package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool

interface AttachmentsRepository {
    suspend fun findAttachment(id: Long): Attachment?
    fun findAttachmentFlow(id: Long): Flow<Attachment?>
    suspend fun getAttachments(): List<Attachment>
    fun getAttachmentsFlow(): Flow<List<Attachment>>

    /**
     * Returns a Flow that emits a value every time the Attachments table changes.
     * This will always emit an initial value on collection.
     */
    fun attachmentsChangeFlow(): Flow<Any?>

    suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean)

    // region Initial Content Methods
    suspend fun storeInitialAttachments(attachments: Collection<Attachment>)
    // endregion Initial Content Methods

    // region Sync Methods
    fun storeAttachmentsFromSync(attachments: Collection<Attachment>)
    fun removeAttachmentsMissingFromSync(toolId: Long, syncedAttachments: Collection<Attachment>)
    fun deleteAttachmentsFor(tool: Tool)
    // endregion Sync Methods
}
