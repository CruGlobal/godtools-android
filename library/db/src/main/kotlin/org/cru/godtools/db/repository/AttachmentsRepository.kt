package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.Attachment

interface AttachmentsRepository {
    fun getAttachmentFlow(id: Long): Flow<Attachment?>
}
