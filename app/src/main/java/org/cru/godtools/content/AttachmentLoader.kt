package org.cru.godtools.content

import android.content.Context
import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.event.content.AttachmentEventBusSubscriber
import org.keynote.godtools.android.db.GodToolsDao

class AttachmentLoader(context: Context) : CachingAsyncTaskEventBusLoader<Attachment>(context) {
    private val dao: GodToolsDao = GodToolsDao.getInstance(context)
    var id = Attachment.INVALID_ID
        set(value) {
            val oldId = field
            field = value
            if (oldId != value) {
                onContentChanged()
            }
        }

    init {
        addEventBusSubscriber(AttachmentEventBusSubscriber(this))
    }

    override fun loadInBackground(): Attachment? {
        return if (id != Attachment.INVALID_ID) dao.find(Attachment::class.java, id) else null
    }
}
