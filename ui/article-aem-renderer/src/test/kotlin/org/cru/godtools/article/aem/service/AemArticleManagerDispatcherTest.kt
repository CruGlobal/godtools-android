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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runCurrent
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.model.Translation
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class AemArticleManagerDispatcherTest {
    private val downloadedTranslationsFlow = MutableSharedFlow<List<Translation>>(extraBufferCapacity = 20)

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
    private val coroutineScope = TestCoroutineScope(SupervisorJob()).apply { pauseDispatcher() }
    private val dao = mockk<GodToolsDao> {
        every { getAsFlow(QUERY_DOWNLOADED_ARTICLE_TRANSLATIONS) } returns downloadedTranslationsFlow
    }
    private val fileManager = mockk<AemArticleManager.FileManager>(relaxUnitFun = true)

    private lateinit var dispatcher: AemArticleManager.Dispatcher

    @Before
    fun setup() {
        dispatcher = AemArticleManager.Dispatcher(aemArticleManager, aemDb, dao, fileManager, coroutineScope)
    }

    @After
    fun cleanup() {
        dispatcher.shutdown()
        coroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun verifyArticleTranslationsJob() {
        dispatcher.cleanupActor.close()
        coroutineScope.resumeDispatcher()
        val translations = emptyList<Translation>()

        assertTrue(downloadedTranslationsFlow.tryEmit(translations))
        coVerifyAll { aemArticleManager.processDownloadedTranslations(translations) }
    }

    // region cleanupActor
    @Test
    fun `cleanupActor - Runs after pre-set delays`() {
        coroutineScope.advanceTimeBy(CLEANUP_DELAY_INITIAL - 1)
        verify { fileManager wasNot Called }
        coroutineScope.advanceTimeBy(1)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        coroutineScope.advanceTimeBy(CLEANUP_DELAY - 1)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }
        coroutineScope.advanceTimeBy(1)
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
        confirmVerified(fileManager)
    }

    @Test
    fun `cleanupActor - Runs after db invalidation`() {
        val observer = aemDbObservers.first()

        // multiple invalidations should be conflated to a single invalidation
        verify { fileManager wasNot Called }
        repeat(10) { observer.onInvalidated(setOf(Resource.TABLE_NAME)) }
        coroutineScope.runCurrent()
        assertEquals(0, coroutineScope.currentTime)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }

        // any invalidations should reset the cleanup delay counter
        coroutineScope.advanceTimeBy(CLEANUP_DELAY - 1)
        coVerify(exactly = 1) { fileManager.removeOrphanedFiles() }
        coroutineScope.advanceTimeBy(1)
        coVerify(exactly = 2) { fileManager.removeOrphanedFiles() }
        confirmVerified(fileManager)
    }
    // endregion cleanupActor
}
