package org.cru.godtools.db.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cru.godtools.base.FileSystem
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
    suspend fun storeAttachmentsFromSync(tool: Tool? = null, attachments: Collection<Attachment>)
    fun deleteAttachmentsFor(tool: Tool)
    // endregion Sync Methods
}

@Composable
fun AttachmentsRepository.rememberAttachmentFile(fileSystem: FileSystem, attachmentId: Long?) =
    remember(fileSystem, attachmentId) {
        when {
            attachmentId != null -> findAttachmentFlow(attachmentId)
                .map { it?.takeIf { it.isDownloaded }?.getFile(fileSystem) }
            else -> flowOf(null)
        }
    }.collectAsState(null).value
