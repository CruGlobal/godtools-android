package org.cru.godtools.article.aem.service

import androidx.room.InvalidationTracker
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import java.security.MessageDigest
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assume.assumeNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mockStatic

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPathApi::class)
class AemArticleManagerTest {
    private val testDir = createTempDirectory().toFile()

    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var fileManager: AemFileManager
    private lateinit var testScope: TestCoroutineScope

    private lateinit var articleManager: KotlinAemArticleManager

    @Before
    fun setup() {
        aemDb = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        fileManager = spy(AemFileManager(mock { on { filesDir } doReturn testDir }))
        testScope = TestCoroutineScope().apply { pauseDispatcher() }

        articleManager = KotlinAemArticleManager(aemDb, fileManager, testScope)
    }

    @After
    fun cleanup() {
        runBlocking { articleManager.shutdown() }
        testScope.cleanupTestCoroutines()
        testDir.deleteRecursively()
    }

    // region InputStream.writeToDisk()
    @Test
    fun testWriteToDisk() {
        val data = "testWriteToDisk()"
        val file = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file!!.readBytes())
    }

    @Test
    fun testWriteToDiskDedup() {
        assumeNotNull(MessageDigest.getInstance("SHA-1"))
        val data = "testWriteToDiskDedup()"
        val digestName = MessageDigest.getInstance("SHA-1").digest(data.toByteArray())
            .joinToString("", postfix = ".bin") { String.format("%02x", it) }

        val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
        val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }

        assertEquals(file1, file2)
        assertEquals(digestName, file1!!.name)
    }

    @Test
    fun testWriteToDiskNoDedupWithoutDigest() {
        mockStatic(MessageDigest::class.java).use {
            it.`when`<MessageDigest?> { MessageDigest.getInstance("SHA-1") } doReturn null

            val data = "testWriteToDiskNoDedupWithoutDigest()"

            val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }
            val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk() } }

            assertNotEquals(file1, file2)
            assertNotEquals(file1!!.name, file2!!.name)
            assertThat(file1.name, startsWith("aem-"))
            assertThat(file2.name, startsWith("aem-"))
        }
    }
    // endregion InputStream.writeToDisk()

    // region cleanupActor
    @Test
    fun verifyCleanupActorRunsAfterDelay() {
        testScope.advanceTimeBy(CLEANUP_DELAY_INITIAL - 1)
        assertCleanupActorRan(times = 0)
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()

        testScope.advanceTimeBy(CLEANUP_DELAY - 1)
        assertCleanupActorRan(times = 0)
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()
    }

    @Test
    fun verifyCleanupActorRunsAfterDbInvalidation() {
        val captor = argumentCaptor<InvalidationTracker.Observer>()
        verify(aemDb.invalidationTracker).addObserver(captor.capture())
        val observer = captor.firstValue

        // multiple invalidations should be conflated to a single invalidation
        assertCleanupActorRan(times = 0)
        repeat(10) { observer.onInvalidated(setOf(Resource.TABLE_NAME)) }
        testScope.runCurrent()
        assertCleanupActorRan()
        assertEquals(0, testScope.currentTime)

        // any invalidations should reset the cleanup delay counter
        testScope.advanceTimeBy(CLEANUP_DELAY - 1)
        assertCleanupActorRan(times = 0)
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()
    }

    private fun assertCleanupActorRan(times: Int = 1) {
        val resourcesDao = aemDb.resourceDao()
        inOrder(resourcesDao, fileManager) {
            repeat(times) {
                verify(resourcesDao).getAll()
                runBlocking { verify(fileManager).getDir() }
            }
            verifyNoMoreInteractions()
        }
        clearInvocations(resourcesDao, fileManager)
    }
    // endregion cleanupActor

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, AemArticleManager.roundTimestamp(15234, 1000))
    }
}
