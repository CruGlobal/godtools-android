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
    private val toolsSemaphore = Semaphore(1)
    private val languagesSemaphore = Semaphore(1)
    private val translationsSemaphore = Semaphore(1)

    private val tasks: Tasks = mockk {
        coEvery { loadBundledTools() } coAnswers {
            toolsSemaphore.acquire()
            emptyList()
        }
        coEvery { loadBundledLanguages() } coAnswers { languagesSemaphore.acquire() }
        coEvery { loadBundledAttachments(any()) } just Runs
        coEvery { loadBundledTranslations(any()) } coAnswers { translationsSemaphore.acquire() }
        coEvery { initFavoriteTools() } just Runs
        coEvery { importBundledAttachments() } just Runs
        coEvery { importBundledTranslations() } just Runs
    }

    @Test
    fun `Verify All Tasks run`() = runTest {
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
    fun `Favorite Tools - Dependent on Translations being loaded`() = runTest {
        translationsSemaphore.acquire()
        InitialContentImporter(tasks, UnconfinedTestDispatcher(testScheduler))

        coVerify { tasks.loadBundledTranslations(any()) }
        coVerify(exactly = 0) { tasks.initFavoriteTools() }

        translationsSemaphore.release()
        coVerifyOrder {
            tasks.loadBundledTranslations(any())
            tasks.initFavoriteTools()
        }
    }

    @Test
    fun `Bundled Attachments - Dependent on Tools being loaded`() = runTest {
        toolsSemaphore.acquire()
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
        toolsSemaphore.acquire()
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
        languagesSemaphore.acquire()
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
