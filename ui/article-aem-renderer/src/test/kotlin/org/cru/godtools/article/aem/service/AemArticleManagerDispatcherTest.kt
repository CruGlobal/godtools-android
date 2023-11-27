package org.cru.godtools.article.aem.service

import androidx.room.InvalidationTracker
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTranslation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AemArticleManagerDispatcherTest {
    private val translationsFlow = MutableStateFlow(emptyList<Translation>())

    private val aemArticleManager = mockk<AemArticleManager>(relaxUnitFun = true)
    private val aemDbObservers = mutableListOf<InvalidationTracker.Observer>()
    private val aemDb = mockk<ArticleRoomDatabase> {
        every { invalidationTracker } returns mockk {
            every { addObserver(capture(aemDbObservers)) } just Runs
        }
        every { aemImportDao() } returns mockk {
            coEvery { getAll() } returns emptyList()
        }
    }
    private val fileManager = mockk<AemArticleManager.FileManager>(relaxUnitFun = true)
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns flowOf(emptyList())
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsForToolsFlow(any()) } returns translationsFlow
    }

    @Before
    fun startDispatcher() {
        AemArticleManager.Dispatcher(
            aemArticleManager,
            aemDb,
            fileManager,
            toolsRepository,
            translationsRepository = translationsRepository,
            coroutineScope = testScope.backgroundScope,
        )
    }

    @Test
    fun verifyArticleTranslationsJob() = testScope.runTest {
        val translations = listOf(randomTranslation(isDownloaded = true))

        translationsFlow.value = translations
        runCurrent()
        coVerifyAll {
            aemArticleManager.processDownloadedTranslations(translations)
        }
    }

    // region cleanupActor
    @Test
    fun `cleanupActor - Runs after pre-set delays`() = testScope.runTest {
        advanceTimeBy(CLEANUP_DELAY_INITIAL)
        verify { fileManager wasNot Called }
        runCurrent()
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        advanceTimeBy(CLEANUP_DELAY)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }
        runCurrent()
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
        confirmVerified(fileManager)
    }

    @Test
    fun `cleanupActor - Runs after db invalidation`() = testScope.runTest {
        // make sure we don't have any runs to start with
        runCurrent()
        verify { fileManager wasNot Called }

        // invalidation before the initial delay should trigger orphaned files cleanup
        aemDbObservers[0].onInvalidated(setOf(Resource.TABLE_NAME))
        runCurrent()
        assertEquals(0, currentTime)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        // invalidations should reset the cleanup delay counter
        advanceTimeBy(CLEANUP_DELAY)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }
        runCurrent()
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }

        // verify there were no other calls made to fileManager
        confirmVerified(fileManager)
    }

    @Test
    fun `cleanupActor - Conflate pending invalidations`() = testScope.runTest {
        // we use a rendezvous channel to control execution of removeOrphanedFiles()
        val removeOrphanedFilesResponse = Channel<Unit>()
        coEvery { fileManager.removeOrphanedFiles() } coAnswers { removeOrphanedFilesResponse.receive() }

        // trigger initial removeOrphanedFiles()
        advanceTimeBy(CLEANUP_DELAY_INITIAL)
        runCurrent()
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        // trigger multiple invalidations while first removeOrphanedFiles is suspended
        repeat(10) { aemDbObservers[0].onInvalidated(setOf(Resource.TABLE_NAME)) }
        removeOrphanedFilesResponse.send(Unit)

        // verify that a second execution of removeOrphanedFiles() triggers immediately
        runCurrent()
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
        removeOrphanedFilesResponse.send(Unit)

        // verify that no more executions of removeOrphanedFiles() happen
        runCurrent()
        assertTrue(removeOrphanedFilesResponse.trySend(Unit).isFailure)
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
    }
    // endregion cleanupActor
}
