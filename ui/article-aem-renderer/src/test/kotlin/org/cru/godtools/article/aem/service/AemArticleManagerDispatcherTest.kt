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
import org.junit.Assert.assertEquals
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
        every { getToolsFlow() } returns flowOf(emptyList())
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowFor(tools = any()) } returns translationsFlow
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
        val translations = listOf(Translation().apply { isDownloaded = true })

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
        val observer = aemDbObservers.first()

        // multiple invalidations should be conflated to a single invalidation
        verify { fileManager wasNot Called }
        repeat(10) { observer.onInvalidated(setOf(Resource.TABLE_NAME)) }
        runCurrent()
        assertEquals(0, currentTime)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        // any invalidations should reset the cleanup delay counter
        advanceTimeBy(CLEANUP_DELAY)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }
        runCurrent()
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
        confirmVerified(fileManager)
    }
    // endregion cleanupActor
}
