package org.cru.godtools.db.room.entity.partial

import org.cru.godtools.model.Attachment

internal class SyncAttachment(attachment: Attachment) {
    val id = attachment.id
    val tool = attachment.toolCode
    val filename = attachment.filename
    val sha256 = attachment.sha256
}
