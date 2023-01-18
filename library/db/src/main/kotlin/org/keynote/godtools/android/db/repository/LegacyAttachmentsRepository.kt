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
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findAsFlow
import org.ccci.gto.android.common.db.findAsync
import org.ccci.gto.android.common.db.get
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class LegacyAttachmentsRepository @Inject constructor(private val dao: GodToolsDao) : AttachmentsRepository {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    override suspend fun findAttachment(id: Long) = dao.findAsync<Attachment>(id).await()

    private val attachmentsCache = WeakLruCache<Long, Flow<Attachment?>>(15)
    override fun findAttachmentFlow(id: Long) = attachmentsCache.getOrPut(id) {
        dao.findAsFlow<Attachment>(id)
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)
    }

    override suspend fun getAttachments() = dao.getAsync(Query.select<Attachment>()).await()
    override fun getAttachmentsFlow() = dao.getAsFlow(Query.select<Attachment>())

    override suspend fun updateAttachmentDownloaded(id: Long, isDownloaded: Boolean) {
        val attachment = Attachment().apply {
            this.id = id
            this.isDownloaded = isDownloaded
        }
        dao.updateAsync(attachment, AttachmentTable.COLUMN_DOWNLOADED).await()
    }

    // region Sync Methods
    override fun storeAttachmentsFromSync(attachments: Collection<Attachment>) {
        attachments.forEach {
            dao.updateOrInsert(
                it,
                AttachmentTable.COLUMN_TOOL, AttachmentTable.COLUMN_FILENAME, AttachmentTable.COLUMN_SHA256
            )
        }
    }

    override fun removeAttachmentsMissingFromSync(toolId: Long, syncedAttachments: Collection<Attachment>) {
        val keep = syncedAttachments.mapTo(mutableSetOf()) { it.id }
        Query.select<Attachment>().where(AttachmentTable.FIELD_TOOL.eq(toolId)).get(dao)
            .filterNot { it.id in keep }
            .forEach { dao.delete(it) }
    }

    override fun deleteAttachmentsFor(tool: Tool) {
        dao.delete(Attachment::class.java, AttachmentTable.FIELD_TOOL.eq(tool.id))
    }
    // endregion Sync Methods
}
