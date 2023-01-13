package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment

interface AttachmentsRepository {
    fun findAttachmentFlow(id: Long): Flow<Attachment?>

    // TODO: temporary for testing
    fun insert(vararg attachments: Attachment)
}
