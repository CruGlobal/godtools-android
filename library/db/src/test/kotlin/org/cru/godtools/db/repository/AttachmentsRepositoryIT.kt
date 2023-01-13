package org.cru.godtools.db.repository

import app.cash.turbine.test
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
        repository.insert(attachment)
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

            repository.insert(attachment)
            assertEquals(attachment.filename, assertNotNull(awaitItem()).filename)
        }
    }

    @Test
    fun `updateAttachmentDownloaded()`() = testScope.runTest {
        repository.insert(
            Attachment().apply {
                id = 1
                isDownloaded = false
            }
        )
        assertFalse(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, true)
        assertTrue(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, true)
        assertTrue(assertNotNull(repository.findAttachment(1)).isDownloaded)

        repository.updateAttachmentDownloaded(1, false)
        assertFalse(assertNotNull(repository.findAttachment(1)).isDownloaded)
    }
}
