package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AttachmentsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: AttachmentsRepository

    @Test
    fun `findAttachment()`() = testScope.runTest {
        assertNull(repository.findAttachment(1))

        val attachment = Attachment().apply {
            id = 1
            filename = "test.ext"
        }
        repository.storeAttachmentsFromSync(listOf(attachment))
        assertEquals(attachment.filename, assertNotNull(repository.findAttachment(1)).filename)
    }

    @Test
    fun `findAttachmentFlow()`() = testScope.runTest {
        val attachment = Attachment().apply {
            id = 1
            filename = "test.ext"
        }

        repository.findAttachmentFlow(1).test {
            assertNull(awaitItem())

            repository.storeAttachmentsFromSync(listOf(attachment))
            assertEquals(attachment.filename, assertNotNull(awaitItem()).filename)
        }
    }

    @Test
    fun `getAttachments()`() = testScope.runTest {
        repository.storeAttachmentsFromSync(
            listOf(
                Attachment().apply {
                    id = 1
                    filename = "name1.bin"
                    isDownloaded = true
                },
                Attachment().apply {
                    id = 2
                    filename = "name2.bin"
                }
            )
        )

        val attachments = repository.getAttachments()
        assertEquals(2, attachments.size)
        val attachment1 = attachments.first { it.id == 1L }
        val attachment2 = attachments.first { it.id == 2L }
        assertEquals("name1.bin", attachment1.filename)
        assertTrue(attachment1.isDownloaded)
        assertEquals("name2.bin", attachment2.filename)
        assertFalse(attachment2.isDownloaded)
    }

    @Test
    fun `updateAttachmentDownloaded()`() = testScope.runTest {
        repository.storeAttachmentsFromSync(
            listOf(
                Attachment().apply {
                    id = 1
                    isDownloaded = false
                }
            )
        )
        assertFalse(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, true)
        assertTrue(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, true)
        assertTrue(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, false)
        assertFalse(assertNotNull(repository.findAttachment(1)).isDownloaded)
    }

    // region storeAttachmentsFromSync()
    @Test
    fun `storeAttachmentsFromSync() - Update existing attachment`() = testScope.runTest {
        val attachment = Attachment().apply {
            id = Random.nextLong()
            filename = "initial.ext"
            sha256 = "initial"
        }

        repository.storeAttachmentsFromSync(listOf(attachment))
        assertNotNull(repository.findAttachment(attachment.id)) {
            assertEquals("initial.ext", it.filename)
            assertEquals("initial", it.sha256)
        }

        attachment.filename = "updated.ext"
        attachment.sha256 = "updated"
        repository.storeAttachmentsFromSync(listOf(attachment))
        assertNotNull(repository.findAttachment(attachment.id)) {
            assertEquals("updated.ext", it.filename)
            assertEquals("updated", it.sha256)
        }
    }

    @Test
    fun `storeAttachmentsFromSync() - Don't overwrite downloaded flag`() = testScope.runTest {
        val attachment = Attachment().apply {
            id = Random.nextLong()
            filename = "file.ext"
            sha256 = "file"
        }

        repository.storeAttachmentsFromSync(listOf(attachment))
        assertFalse(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
        repository.updateAttachmentDownloaded(attachment.id, true)
        assertTrue(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
        repository.storeAttachmentsFromSync(listOf(attachment))
        assertTrue(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
    }
    // endregion storeAttachmentsFromSync()

    // region removeAttachmentsMissingFromSync()
    @Test
    fun `removeAttachmentsMissingFromSync()`() = testScope.runTest {
        val attachment1 = Attachment().apply {
            id = Random.nextLong()
            toolId = 1
        }
        val attachment2 = Attachment().apply {
            id = Random.nextLong()
            toolId = 1
        }
        val attachment3 = Attachment().apply {
            id = Random.nextLong()
            toolId = 2
        }
        repository.storeAttachmentsFromSync(listOf(attachment1, attachment2, attachment3))

        repository.removeAttachmentsMissingFromSync(1, listOf(attachment2))
        assertNotNull(repository.getAttachments()) {
            assertEquals(2, it.size)
            assertEquals(setOf(attachment2.id, attachment3.id), it.map { it.id }.toSet())
        }
    }
    // endregion removeAttachmentsMissingFromSync()

    // region deleteAttachmentsFor()
    @Test
    fun `deleteAttachmentsFor()`() = testScope.runTest {
        val tool = Tool().apply { id = Random.nextLong() }
        val attachments = List(2) { Attachment().apply { id = Random.nextLong() } }
        attachments[0].toolId = tool.id
        repository.storeAttachmentsFromSync(attachments)

        repository.deleteAttachmentsFor(tool)
        assertNotNull(repository.getAttachments()) {
            assertEquals(1, it.size)
            assertEquals(attachments[1].id, it[0].id)
        }
    }
    // endregion deleteAttachmentsFor()
}
