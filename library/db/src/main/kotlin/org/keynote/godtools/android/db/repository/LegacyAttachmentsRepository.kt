package org.keynote.godtools.android.db.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.collection.getOrPut
import org.ccci.gto.android.common.db.findAsFlow
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.model.Attachment
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyAttachmentsRepository @Inject constructor(private val dao: GodToolsDao) : AttachmentsRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    private val attachmentsCache = WeakLruCache<Long, Flow<Attachment?>>(15)
    override fun getAttachmentFlow(id: Long) = attachmentsCache.getOrPut(id) {
        dao.findAsFlow<Attachment>(id)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)
    }

    override fun insert(vararg attachments: Attachment) {
        attachments.forEach { dao.insert(it) }
    }
}
