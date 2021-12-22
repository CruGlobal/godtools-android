package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.io.path.ExperimentalPathApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.Mockito.CALLS_REAL_METHODS
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.UseConstructor
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalPathApi::class)
class AemArticleManagerTest {
    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var api: AemApi
    private lateinit var dao: GodToolsDao
    private lateinit var fileManager: AemArticleManager.FileManager
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
        fileManager = mock()
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
    fun testDownloadResource(): Unit = runBlocking {
        val uri = mock<Uri>()
        val resource = Resource(uri)
        val response = ByteArray(0).toResponseBody()
        whenever(aemDb.resourceDao().find(uri)) doReturn resource
        whenever(api.downloadResource(uri)) doReturn Response.success(response)

        articleManager.downloadResource(uri, false)
        verify(aemDb.resourceDao()).find(uri)
        verify(api).downloadResource(uri)
        verify(fileManager).storeResponse(response, resource)
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
    // endregion Download Resource

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, 15234L.roundTimestamp(1000))
    }
}
