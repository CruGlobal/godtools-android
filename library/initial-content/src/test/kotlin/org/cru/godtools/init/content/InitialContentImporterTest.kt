package org.cru.godtools.init.content

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.cru.godtools.init.content.task.Tasks

@OptIn(ExperimentalCoroutinesApi::class)
class InitialContentImporterTest {
    private val toolsSemaphore = Semaphore(1, 1)
    private val languagesSemaphore = Semaphore(1, 1)

    private val tasks: Tasks = mockk {
        coEvery { loadBundledTools() } coAnswers {
            toolsSemaphore.acquire()
            emptyList()
        }
        coEvery { loadBundledLanguages() } coAnswers { languagesSemaphore.acquire() }
        coEvery { loadBundledAttachments(any()) } just Runs
        coEvery { loadBundledTranslations(any()) } just Runs
        coEvery { initFavoriteTools() } just Runs
        coEvery { importBundledAttachments() } just Runs
        coEvery { importBundledTranslations() } just Runs
    }

    @Test
    fun `Verify All Tasks run`() = runTest {
        toolsSemaphore.release()
        languagesSemaphore.release()
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerifyAll {
            tasks.loadBundledLanguages()
            tasks.loadBundledTools()
            tasks.initFavoriteTools()
            tasks.loadBundledAttachments(any())
            tasks.loadBundledTranslations(any())
            tasks.importBundledAttachments()
            tasks.importBundledTranslations()
        }
    }

    @Test
    fun `Favorite Tools - Dependent on Tools being loaded`() = runTest {
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerify { tasks.loadBundledTools() }
        coVerify(exactly = 0) { tasks.initFavoriteTools() }

        toolsSemaphore.release()
        coVerifyOrder {
            tasks.loadBundledTools()
            tasks.initFavoriteTools()
        }
    }

    @Test
    fun `Bundled Attachments - Dependent on Tools being loaded`() = runTest {
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerify { tasks.loadBundledTools() }
        coVerify(exactly = 0) {
            tasks.loadBundledAttachments(any())
            tasks.importBundledAttachments()
        }

        toolsSemaphore.release()
        coVerifyOrder {
            tasks.loadBundledTools()
            tasks.loadBundledAttachments(any())
            tasks.importBundledAttachments()
        }
    }

    @Test
    fun `Bundled Translations - Dependent on Tools being loaded`() = runTest {
        languagesSemaphore.release()
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerify {
            tasks.loadBundledTools()
            tasks.loadBundledLanguages()
        }
        coVerify(exactly = 0) {
            tasks.loadBundledTranslations(any())
            tasks.importBundledTranslations()
        }

        toolsSemaphore.release()
        coVerifyOrder {
            tasks.loadBundledTools()
            tasks.loadBundledTranslations(any())
            tasks.importBundledTranslations()
        }
    }

    @Test
    fun `Bundled Translations - Dependent on Languages being loaded`() = runTest {
        toolsSemaphore.release()
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerify {
            tasks.loadBundledTools()
            tasks.loadBundledLanguages()
        }
        coVerify(exactly = 0) {
            tasks.loadBundledTranslations(any())
            tasks.importBundledTranslations()
        }

        languagesSemaphore.release()
        coVerifyOrder {
            tasks.loadBundledTools()
            tasks.loadBundledTranslations(any())
            tasks.importBundledTranslations()
        }
    }
}
