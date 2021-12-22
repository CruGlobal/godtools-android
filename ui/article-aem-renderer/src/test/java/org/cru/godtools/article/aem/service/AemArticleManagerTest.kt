package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.tool.model.Manifest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class AemArticleManagerTest {
    private lateinit var aemDb: ArticleRoomDatabase
    private lateinit var api: AemApi
    private lateinit var fileManager: AemArticleManager.FileManager
    private lateinit var manifestManager: ManifestManager

    private lateinit var articleManager: AemArticleManager

    @Before
    fun setup() {
        aemDb = mock(defaultAnswer = RETURNS_DEEP_STUBS) {
            onBlocking { aemImportDao().getAll() } doReturn emptyList()
            onBlocking { resourceDao().getAll() } doReturn emptyList()
        }
        api = mock()
        fileManager = mock()
        manifestManager = mock()

        articleManager = mock(
            defaultAnswer = CALLS_REAL_METHODS,
            useConstructor = UseConstructor.withArguments(aemDb, api, fileManager, manifestManager)
        )
    }

    @After
    fun cleanup() {
        runBlocking { articleManager.shutdown() }
    }

    // region Translations
    @Test
    fun `processDownloadedTranslations()`() = runBlocking {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val uri = mock<Uri>()
        val manifest = mock<Manifest> { on { aemImports } doReturn listOf(uri) }
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(any()) } doReturn false
        }
        whenever(manifestManager.getManifest(translation)) doReturn manifest
        whenever(articleManager.syncAemImportsFromManifest(any(), any())) doReturn null

        articleManager.processDownloadedTranslations(translations)
        verify(repository).isProcessed(translation)
        verify(manifestManager).getManifest(translation)
        verify(repository).addAemImports(translation, manifest.aemImports)
        verify(articleManager).syncAemImportsFromManifest(eq(manifest), any())
        verify(repository).removeMissingTranslations(translations)
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `processDownloadedTranslations() - Don't process already processed translations`() = runBlocking {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(translation) } doReturn true
        }

        articleManager.processDownloadedTranslations(translations)
        verify(repository).isProcessed(translation)
        verify(repository).removeMissingTranslations(translations)
        verifyNoInteractions(manifestManager)
        verify(articleManager, never()).syncAemImportsFromManifest(any(), any())
        verifyNoMoreInteractions(repository)
    }

    @Test
    fun `processDownloadedTranslations() - Don't process translations without a downloaded manifest`() = runBlocking {
        val translation = mock<Translation>()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository().stub {
            onBlocking { isProcessed(translation) } doReturn false
        }
        whenever(manifestManager.getManifest(translation)) doReturn null

        articleManager.processDownloadedTranslations(translations)
        verify(repository).isProcessed(translation)
        verify(manifestManager).getManifest(translation)
        verify(repository, never()).addAemImports(any(), any())
        verify(articleManager, never()).syncAemImportsFromManifest(any(), any())
        verify(repository).removeMissingTranslations(translations)
        verifyNoMoreInteractions(repository)
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
