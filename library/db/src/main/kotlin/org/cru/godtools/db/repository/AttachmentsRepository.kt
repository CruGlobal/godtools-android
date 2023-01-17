package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment

interface AttachmentsRepository {
    suspend fun findAttachment(id: Long): Attachment?
    fun findAttachmentFlow(id: Long): Flow<Attachment?>
    suspend fun getAttachments(): List<Attachment>

    suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean)

    // TODO: temporary for testing
    fun insert(vararg attachments: Attachment)
}
