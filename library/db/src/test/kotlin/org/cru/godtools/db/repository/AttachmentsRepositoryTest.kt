package org.cru.godtools.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.model.Attachment
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttachmentsRepositoryTest {
    private val fileSystem: ToolFileSystem = mockk()
    private val repository: AttachmentsRepository = mockk()

    // region rememberAttachmentFile()
    @Test
    fun `rememberAttachmentFile()`() = runTest {
        val attachmentFlow = MutableStateFlow<Attachment?>(null)
        val file = File.createTempFile("prefix", "ext")

        every { repository.findAttachmentFlow(1) } returns attachmentFlow
        coEvery { fileSystem.file("file.ext") } returns file

        moleculeFlow(RecompositionMode.Immediate) {
            repository.rememberAttachmentFile(fileSystem, 1)
        }.test {
            assertNull(expectMostRecentItem())

            attachmentFlow.value = Attachment {
                filename = "file.ext"
                sha256 = "file"
                isDownloaded = true
            }
            assertEquals(file, expectMostRecentItem())
        }
    }

    @Test
    fun `rememberAttachmentFile() - attachment not downloaded`() = runTest {
        every { repository.findAttachmentFlow(1) } returns flowOf(Attachment { isDownloaded = false })

        moleculeFlow(RecompositionMode.Immediate) {
            repository.rememberAttachmentFile(fileSystem, 1)
        }.test {
            assertNull(awaitItem())
        }

        verifyAll {
            repository.findAttachmentFlow(1)
            fileSystem wasNot Called
        }
    }

    @Test
    fun `rememberAttachmentFile() - no attachment`() = runTest {
        every { repository.findAttachmentFlow(1) } returns flowOf(null)

        moleculeFlow(RecompositionMode.Immediate) {
            repository.rememberAttachmentFile(fileSystem, 1)
        }.test {
            assertNull(awaitItem())
        }

        verifyAll {
            repository.findAttachmentFlow(1)
            fileSystem wasNot Called
        }
    }

    @Test
    fun `rememberAttachmentFile() - no attachment id`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            repository.rememberAttachmentFile(fileSystem, null)
        }.test {
            assertNull(awaitItem())
        }

        verifyAll {
            repository wasNot Called
            fileSystem wasNot Called
        }
    }
    // endregion rememberAttachmentFile()
}
