package org.cru.godtools.article.aem.service

import androidx.room.InvalidationTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestCoroutineScope
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class AemArticleManagerDispatcherTest {
    private val aemDb = mock<ArticleRoomDatabase>(defaultAnswer = RETURNS_DEEP_STUBS)
    private val coroutineScope = TestCoroutineScope(SupervisorJob()).apply { pauseDispatcher() }
    private val fileManager = mock<AemArticleManager.FileManager>()

    private lateinit var dispatcher: AemArticleManager.Dispatcher

    @Before
    fun setup() {
        dispatcher = AemArticleManager.Dispatcher(aemDb, fileManager, coroutineScope)
    }

    @After
    fun cleanup() {
        dispatcher.shutdown()
        coroutineScope.cleanupTestCoroutines()
    }

    // region cleanupActor
    @Test
    fun `cleanupActor - Runs after pre-set delays`() {
        coroutineScope.advanceTimeBy(CLEANUP_DELAY_INITIAL - 1)
        verifyBlocking(fileManager, never()) { removeOrphanedFiles() }
        coroutineScope.advanceTimeBy(1)
        verifyBlocking(fileManager) { removeOrphanedFiles() }
        clearInvocations(fileManager)

        coroutineScope.advanceTimeBy(CLEANUP_DELAY - 1)
        verifyBlocking(fileManager, never()) { removeOrphanedFiles() }
        coroutineScope.advanceTimeBy(1)
        verifyBlocking(fileManager) { removeOrphanedFiles() }
    }

    @Test
    fun `cleanupActor - Runs after db invalidation`() {
        val captor = argumentCaptor<InvalidationTracker.Observer>()
        verify(aemDb.invalidationTracker).addObserver(captor.capture())
        val observer = captor.firstValue

        // multiple invalidations should be conflated to a single invalidation
        verifyBlocking(fileManager, never()) { removeOrphanedFiles() }
        repeat(10) { observer.onInvalidated(setOf(Resource.TABLE_NAME)) }
        coroutineScope.runCurrent()
        assertEquals(0, coroutineScope.currentTime)
        verifyBlocking(fileManager) { removeOrphanedFiles() }
        clearInvocations(fileManager)

        // any invalidations should reset the cleanup delay counter
        coroutineScope.advanceTimeBy(CLEANUP_DELAY - 1)
        verifyBlocking(fileManager, never()) { removeOrphanedFiles() }
        coroutineScope.advanceTimeBy(1)
        verifyBlocking(fileManager) { removeOrphanedFiles() }
    }
    // endregion cleanupActor
}
