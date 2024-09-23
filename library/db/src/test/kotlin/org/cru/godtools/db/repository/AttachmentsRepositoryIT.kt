package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.randomTool

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AttachmentsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: AttachmentsRepository
    abstract val toolsRepository: ToolsRepository

    private val tool = randomTool("tool")
    private val tool2 = randomTool("tool2")

    @BeforeTest
    fun createTools() = runBlocking { toolsRepository.storeToolsFromSync(setOf(tool, tool2)) }

    @Test
    fun `findAttachment()`() = testScope.runTest {
        assertNull(repository.findAttachment(1))

        val attachment = Attachment(tool = tool).apply {
            id = 1
            filename = "test.ext"
        }
        repository.storeInitialAttachments(listOf(attachment))
        assertEquals(attachment.filename, assertNotNull(repository.findAttachment(1)).filename)
    }

    @Test
    fun `findAttachmentFlow()`() = testScope.runTest {
        val attachment = Attachment(tool = tool).apply {
            id = 1
            filename = "test.ext"
        }

        repository.findAttachmentFlow(1).test {
            assertNull(awaitItem())

            repository.storeInitialAttachments(listOf(attachment))
            assertEquals(attachment.filename, assertNotNull(awaitItem()).filename)
        }
    }

    @Test
    fun `getAttachments()`() = testScope.runTest {
        repository.storeInitialAttachments(
            listOf(
                Attachment(tool = tool).apply {
                    id = 1
                    filename = "name1.bin"
                    isDownloaded = true
                },
                Attachment(tool = tool).apply {
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

    // region attachmentsChangeFlow()
    @Test
    fun `attachmentsChangeFlow()`() = testScope.runTest {
        repository.attachmentsChangeFlow().test {
            runCurrent()
            expectMostRecentItem()

            val attachment = Attachment(tool = tool).apply {
                id = 1
                filename = "name1.bin"
                isDownloaded = false
            }
            repository.storeInitialAttachments(listOf(attachment))
            runCurrent()
            expectMostRecentItem()

            repository.updateAttachmentDownloaded(attachment.id, true)
            runCurrent()
            expectMostRecentItem()
        }
    }
    // endregion attachmentsChangeFlow()

    @Test
    fun `updateAttachmentDownloaded()`() = testScope.runTest {
        repository.storeInitialAttachments(
            listOf(
                Attachment(tool = tool).apply {
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

    // region storeInitialAttachments()
    @Test
    fun `storeInitialAttachments() - Don't replace already existing attachments`() = testScope.runTest {
        val attachment = Attachment(tool = tool) { filename = "sync.bin" }
        repository.storeAttachmentsFromSync(tool, listOf(attachment))

        attachment.filename = "initial.bin"
        repository.storeInitialAttachments(listOf(attachment))
        assertNotNull(repository.findAttachment(attachment.id)) {
            assertEquals("sync.bin", it.filename)
        }
    }

    @Test
    fun `storeInitialAttachments() - Ignore attachments for tools that don't exist`() = testScope.runTest {
        val missingTool = randomTool("missing")
        val attachment = Attachment(tool = missingTool)

        repository.storeInitialAttachments(listOf(attachment))
        assertNull(repository.findAttachment(attachment.id))
    }
    // endregion storeInitialAttachments()

    // region storeAttachmentsFromSync()
    @Test
    fun `storeAttachmentsFromSync() - Update existing attachment`() = testScope.runTest {
        val attachment = Attachment(tool = tool) {
            filename = "initial.ext"
            sha256 = "initial"
        }

        repository.storeAttachmentsFromSync(tool, listOf(attachment))
        assertNotNull(repository.findAttachment(attachment.id)) {
            assertEquals("initial.ext", it.filename)
            assertEquals("initial", it.sha256)
        }

        attachment.filename = "updated.ext"
        attachment.sha256 = "updated"
        repository.storeAttachmentsFromSync(tool, listOf(attachment))
        assertNotNull(repository.findAttachment(attachment.id)) {
            assertEquals("updated.ext", it.filename)
            assertEquals("updated", it.sha256)
        }
    }

    @Test
    fun `storeAttachmentsFromSync() - Don't overwrite downloaded flag`() = testScope.runTest {
        val attachment = Attachment(tool = tool) {
            filename = "file.ext"
            sha256 = "file"
        }

        repository.storeAttachmentsFromSync(tool, listOf(attachment))
        assertFalse(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
        repository.updateAttachmentDownloaded(attachment.id, true)
        assertTrue(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
        repository.storeAttachmentsFromSync(tool, listOf(attachment))
        assertTrue(assertNotNull(repository.findAttachment(attachment.id)).isDownloaded)
    }

    @Test
    fun `storeAttachmentsFromSync() - remove stale attachments`() = testScope.runTest {
        val attachment = Attachment(tool = tool)
        val attachmentStale = Attachment(tool = tool)
        val attachmentNew = Attachment(tool = tool)
        val attachmentOtherTool = Attachment(tool = tool2)
        toolsRepository.storeToolsFromSync(listOf(tool, tool2))
        repository.storeInitialAttachments(listOf(attachment, attachmentStale, attachmentOtherTool))

        repository.storeAttachmentsFromSync(tool, listOf(attachment, attachmentNew))
        assertNotNull(repository.getAttachments()) {
            assertEquals(3, it.size)
            assertEquals(setOf(attachment.id, attachmentNew.id, attachmentOtherTool.id), it.map { it.id }.toSet())
        }
    }
    // endregion storeAttachmentsFromSync()

    // region deleteAttachmentsFor()
    @Test
    fun `deleteAttachmentsFor()`() = testScope.runTest {
        val attachments = listOf(
            Attachment(tool = tool),
            Attachment(tool = tool2)
        )
        repository.storeInitialAttachments(attachments)

        repository.deleteAttachmentsFor(tool)
        assertNotNull(repository.getAttachments()) {
            assertEquals(1, it.size)
            assertEquals(attachments[1].id, it[0].id)
        }
    }
    // endregion deleteAttachmentsFor()
}
