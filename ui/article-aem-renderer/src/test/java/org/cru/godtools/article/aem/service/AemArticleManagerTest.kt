package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.room.InvalidationTracker
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.UseConstructor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyBlocking
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.io.File
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
import okhttp3.ResponseBody
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.article.aem.util.AemFileManager
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.xml.model.Manifest
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
import org.mockito.verification.VerificationMode
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPathApi::class)
class AemArticleManagerTest {
    private val testDir = createTempDirectory().toFile()

    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var api: AemApi
    private lateinit var dao: GodToolsDao
    private lateinit var fileManager: AemFileManager
    private lateinit var manifestManager: ManifestManager
    private lateinit var testScope: TestCoroutineScope

    private lateinit var articleManager: AemArticleManager

    private val articleTranslationsChannel = Channel<List<Translation>>()

    @Before
    fun setup() {
        aemDb = mock(defaultAnswer = RETURNS_DEEP_STUBS) {
            onBlocking { aemImportDao().getAll() } doReturn emptyList()
        }
        api = mock()
        dao = mock {
            on { getAsFlow(QUERY_ARTICLE_TRANSLATIONS) } doReturn articleTranslationsChannel.consumeAsFlow()
        }
        fileManager = spy(AemFileManager(mock { on { filesDir } doReturn testDir }))
        manifestManager = mock()
        testScope = TestCoroutineScope().apply { pauseDispatcher() }

        articleManager = mock(
            defaultAnswer = CALLS_REAL_METHODS,
            useConstructor = UseConstructor.withArguments(aemDb, api, dao, fileManager, manifestManager, testScope)
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
        val manifest = Manifest(aemImports = listOf(uri))
        val repository = aemDb.translationRepository()
        stub {
            onBlocking { manifestManager.getManifest(translation) } doReturn manifest
            onBlocking { articleManager.syncAemImportsFromManifest(any(), any()) } doReturn null
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verify(repository).isProcessed(translation)
        verifyBlocking(manifestManager) { getManifest(translation) }
        verify(repository).addAemImports(translation, manifest.aemImports)
        verifyBlocking(articleManager) { syncAemImportsFromManifest(eq(manifest), any()) }
        verify(repository).removeMissingTranslations(translations)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `testArticleTranslationsJob - Don't process translations that have already been processed`() {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository().stub {
            on { isProcessed(translation) } doReturn true
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verify(repository).isProcessed(translation)
        verify(repository).removeMissingTranslations(translations)
        verifyNoInteractions(manifestManager)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `testArticleTranslationsJob - Don't process translations without a downloaded manifest`() {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository()
        manifestManager.stub {
            onBlocking { getManifest(translation) } doReturn null
        }

        startArticleTranslationsJob()
        translations.offerToArticleTranslationsJob()
        testScope.runCurrent()

        verify(repository).isProcessed(translation)
        verifyBlocking(manifestManager) { getManifest(translation) }
        verify(repository, never()).addAemImports(any(), any())
        verify(repository).removeMissingTranslations(translations)
        verifyNoMoreInteractions(repository)
    }

    private fun startArticleTranslationsJob() {
        testScope.runCurrent()
        verify(dao).getAsFlow(QUERY_ARTICLE_TRANSLATIONS)
    }

    private fun List<Translation>.offerToArticleTranslationsJob() = apply {
        assertTrue("Unable to trigger articleTranslationsJob", articleTranslationsChannel.offer(this))
    }
    // endregion Translations

    // region Download Resource
    @Test
    fun testDownloadResource() = runBlocking {
        val resourceDao = aemDb.resourceDao()
        val data = "testDownloadResource()"
        val uri = mock<Uri>()
        val mediaType = MediaType.get("image/jpg")
        val resource = mock<Resource> { on { needsDownload() } doReturn true }
        whenever(resourceDao.find(uri)).thenReturn(resource)
        wheneverDownloadingResource(uri)
            .thenReturn(Response.success(ResponseBody.create(mediaType, data.toByteArray())))

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
        val file = runBlocking { fileManager.getFile(fileName.firstValue) }
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
        lateinit var file: File
        with(articleManager) { data.byteInputStream().use { it.writeToDisk { file = it } } }
        assertNotNull(file)
        assertArrayEquals(data.toByteArray(), file.readBytes())
    }

    @Test
    fun testWriteToDiskDedup() = runBlocking {
        val data = "testWriteToDiskDedup()"

        lateinit var file1: File
        lateinit var file2: File
        with(articleManager) { data.byteInputStream().use { it.writeToDisk { file1 = it } } }
        with(articleManager) { data.byteInputStream().use { it.writeToDisk { file2 = it } } }

        assertEquals(file1, file2)
    }

    @Test
    fun testWriteToDiskNoDedupWithoutDigest() = runBlocking {
        mockStatic(MessageDigest::class.java).use {
            it.`when`<MessageDigest?> { MessageDigest.getInstance("SHA-1") } doReturn null

            val data = "testWriteToDiskNoDedupWithoutDigest()"

            lateinit var file1: File
            lateinit var file2: File
            with(articleManager) { data.byteInputStream().use { it.writeToDisk { file1 = it } } }
            with(articleManager) { data.byteInputStream().use { it.writeToDisk { file2 = it } } }

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
        runBlocking { whenever(fileManager.createDir()) doReturn true }
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
        verifyBlocking(fileManager, mode) { createDir() }
        verify(resourceDao, mode).getAll()
        verifyBlocking(fileManager, mode) { getDir() }
        verifyNoMoreInteractions(resourceDao, fileManager)
        clearInvocations(resourceDao, fileManager)
    }
    // endregion cleanupActor

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, 15234L.roundTimestamp(1000))
    }
}
