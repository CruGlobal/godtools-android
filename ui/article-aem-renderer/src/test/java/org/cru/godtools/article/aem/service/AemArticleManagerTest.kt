package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.room.InvalidationTracker
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.security.MessageDigest
import java.util.Date
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileSystem
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThanOrEqualTo
import org.hamcrest.Matchers.startsWith
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.UseConstructor
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.mockito.verification.VerificationMode
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPathApi::class)
class AemArticleManagerTest {
    private val testDir = createTempDirectory().toFile()

    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var api: AemApi
    private lateinit var dao: GodToolsDao
    private lateinit var fs: AemFileSystem
    private lateinit var manifestManager: ManifestManager
    private lateinit var testScope: TestCoroutineScope

    private lateinit var articleManager: AemArticleManager

    private val articleTranslationsChannel = Channel<List<Translation>>()

    @Before
    fun setup() {
        aemDb = mock(defaultAnswer = RETURNS_DEEP_STUBS) {
            onBlocking { aemImportDao().getAll() } doReturn emptyList()
            onBlocking { resourceDao().getAll() } doReturn emptyList()
        }
        api = mock()
        dao = mock {
            on { getAsFlow(QUERY_ARTICLE_TRANSLATIONS) } doReturn articleTranslationsChannel.consumeAsFlow()
        }
        fs = spy(AemFileSystem(mock { on { filesDir } doReturn testDir }))
        manifestManager = mock()
        testScope = TestCoroutineScope().apply { pauseDispatcher() }

        articleManager = mock(
            defaultAnswer = CALLS_REAL_METHODS,
            useConstructor = UseConstructor.withArguments(aemDb, api, dao, fs, manifestManager, testScope)
        )
    }

    @After
    fun cleanup() {
        articleTranslationsChannel.close()
        runBlocking { articleManager.shutdown() }
        testScope.cleanupTestCoroutines()
        testDir.deleteRecursively()
    }

    // region Translations
    @Test
    fun testArticleTranslationsJob() {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val uri = mock<Uri>()
        val manifest = mock<Manifest> { on { aemImports } doReturn listOf(uri) }
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(any()) } doReturn false
        }
        stub {
            onBlocking { manifestManager.getManifest(translation) } doReturn manifest
            onBlocking { articleManager.syncAemImportsFromManifest(any(), any()) } doReturn null
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verifyBlocking(repository) { isProcessed(translation) }
        verifyBlocking(manifestManager) { getManifest(translation) }
        verifyBlocking(repository) { addAemImports(translation, manifest.aemImports) }
        verifyBlocking(articleManager) { syncAemImportsFromManifest(eq(manifest), any()) }
        verifyBlocking(repository) { removeMissingTranslations(translations) }
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `testArticleTranslationsJob - Don't process translations that have already been processed`() {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(translation) } doReturn true
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verifyBlocking(repository) { isProcessed(translation) }
        verifyBlocking(repository) { removeMissingTranslations(translations) }
        verifyNoInteractions(manifestManager)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `testArticleTranslationsJob - Don't process translations without a downloaded manifest`() {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(translation) } doReturn false
        }
        manifestManager.stub {
            onBlocking { getManifest(translation) } doReturn null
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verifyBlocking(repository) { isProcessed(translation) }
        verifyBlocking(manifestManager) { getManifest(translation) }
        verifyBlocking(repository, never()) { addAemImports(any(), any()) }
        verifyBlocking(repository) { removeMissingTranslations(translations) }
        verifyNoMoreInteractions(repository)
    }

    private fun startArticleTranslationsJob() {
        testScope.runCurrent()
        verify(dao).getAsFlow(QUERY_ARTICLE_TRANSLATIONS)
    }

    private fun List<Translation>.offerToArticleTranslationsJob() = apply {
        assertTrue("Unable to trigger articleTranslationsJob", articleTranslationsChannel.trySend(this).isSuccess)
    }
    // endregion Translations

    // region Download Resource
    @Test
    fun testDownloadResource() = runBlocking {
        val resourceDao = aemDb.resourceDao()
        val data = "testDownloadResource()"
        val uri = mock<Uri>()
        val mediaType = "image/jpg".toMediaType()
        val resource = mock<Resource> { on { needsDownload() } doReturn true }
        whenever(resourceDao.find(uri)).thenReturn(resource)
        wheneverDownloadingResource(uri).thenReturn(Response.success(data.toByteArray().toResponseBody(mediaType)))

        val startTime = System.currentTimeMillis()
        articleManager.downloadResource(uri, false)
        val endTime = System.currentTimeMillis()
        verify(aemDb.resourceDao()).find(uri)
        verifyBlocking(api) { downloadResource(uri) }

        val type = argumentCaptor<MediaType>()
        val fileName = argumentCaptor<String>()
        val date = argumentCaptor<Date>()
        verify(resourceDao).updateLocalFile(eq(uri), type.capture(), fileName.capture(), date.capture())
        assertEquals(mediaType, type.firstValue)
        assertThat(date.firstValue.time, allOf(greaterThanOrEqualTo(startTime), lessThanOrEqualTo(endTime)))
        val file = fs.file(fileName.firstValue)
        assertArrayEquals(data.toByteArray(), file.readBytes())
    }

    @Test
    fun `Don't download resource if it doesn't exist in the database`() = runBlocking {
        val uri = mock<Uri>()
        articleManager.downloadResource(uri, false)
        verify(aemDb.resourceDao()).find(uri)
        verifyNoInteractions(api)
    }

    @Test
    fun `Don't download resource if it has already been downloaded`() = runBlocking {
        val uri = mock<Uri>()
        val resource = mock<Resource> { on { needsDownload() } doReturn false }
        whenever(aemDb.resourceDao().find(uri)).thenReturn(resource)

        articleManager.downloadResource(uri, false)
        verify(aemDb.resourceDao()).find(uri)
        verify(resource).needsDownload()
        verifyNoInteractions(api)
    }

    private fun wheneverDownloadingResource(uri: Uri = any()) = runBlocking { whenever(api.downloadResource(uri)) }

    // region InputStream.writeToDisk()
    @Test
    fun testWriteToDisk() = runBlocking {
        val data = "testWriteToDisk()"

        val file = with(articleManager) { data.byteInputStream().use { it.writeToDisk()!! } }
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file.readBytes())
    }

    @Test
    fun testWriteToDiskDedup() = runBlocking {
        val data = "testWriteToDiskDedup()"

        val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk()!! } }
        val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk()!! } }

        assertEquals(file1, file2)
    }

    @Test
    fun testWriteToDiskNoDedupWithoutDigest() = runBlocking {
        mockStatic(MessageDigest::class.java).use {
            it.`when`<MessageDigest?> { MessageDigest.getInstance("SHA-1") } doReturn null
            val data = "testWriteToDiskNoDedupWithoutDigest()"

            val file1 = with(articleManager) { data.byteInputStream().use { it.writeToDisk()!! } }
            val file2 = with(articleManager) { data.byteInputStream().use { it.writeToDisk()!! } }

            assertNotEquals(file1, file2)
            assertNotEquals(file1.name, file2.name)
            assertThat(file1.name, startsWith("aem-"))
            assertThat(file2.name, startsWith("aem-"))
        }
    }
    // endregion InputStream.writeToDisk()
    // endregion Download Resource

    // region cleanupActor
    private fun setupCleanupActor() {
        runBlocking { whenever(fs.exists()) doReturn true }
    }

    @Test
    fun `testCleanupActor - Runs after pre-set delays`() {
        setupCleanupActor()

        testScope.advanceTimeBy(CLEANUP_DELAY_INITIAL - 1)
        assertCleanupActorRan(never())
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()

        testScope.advanceTimeBy(CLEANUP_DELAY - 1)
        assertCleanupActorRan(never())
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()
    }

    @Test
    fun `testCleanupActor - Runs after db invalidation`() {
        setupCleanupActor()
        val captor = argumentCaptor<InvalidationTracker.Observer>()
        verify(aemDb.invalidationTracker).addObserver(captor.capture())
        val observer = captor.firstValue

        // multiple invalidations should be conflated to a single invalidation
        assertCleanupActorRan(never())
        repeat(10) { observer.onInvalidated(setOf(Resource.TABLE_NAME)) }
        testScope.runCurrent()
        assertCleanupActorRan()
        assertEquals(0, testScope.currentTime)

        // any invalidations should reset the cleanup delay counter
        testScope.advanceTimeBy(CLEANUP_DELAY - 1)
        assertCleanupActorRan(never())
        testScope.advanceTimeBy(1)
        assertCleanupActorRan()
    }

    private fun assertCleanupActorRan(mode: VerificationMode = times(1)) {
        val resourceDao = aemDb.resourceDao()
        verifyBlocking(fs, mode) { exists() }
        verifyBlocking(resourceDao, mode) { getAll() }
        verifyBlocking(fs, mode) { rootDir() }
        verifyNoMoreInteractions(resourceDao, fs)
        clearInvocations(resourceDao, fs)
    }
    // endregion cleanupActor

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, 15234L.roundTimestamp(1000))
    }
}
