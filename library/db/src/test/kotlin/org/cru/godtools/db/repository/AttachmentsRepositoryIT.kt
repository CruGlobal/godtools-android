package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Attachment

@OptIn(ExperimentalCoroutinesApi::class)
abstract class AttachmentsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: AttachmentsRepository

    @Test
    fun `findAttachmentFlow()`() = testScope.runTest {
        val attachment = Attachment().apply {
            id = 1
            filename = "test.ext"
        }

        repository.getAttachmentFlow(1).test {
            assertNull(awaitItem())

            repository.insert(attachment)
            assertEquals(attachment.filename, assertNotNull(awaitItem()).filename)
        }
    }
}
