package org.cru.godtools.article.aem.service

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.article.aem.api.AemApi
import org.cru.godtools.article.aem.db.ArticleRoomDatabase
import org.cru.godtools.article.aem.db.ResourceDao
import org.cru.godtools.article.aem.model.Resource
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class AemArticleManagerTest {
    private val resourceDao = mockk<ResourceDao> {
        coEvery { find(any()) } returns null
        coEvery { getAll() } returns emptyList()
    }
    private val aemDb = mockk<ArticleRoomDatabase> {
        every { resourceDao() } returns resourceDao
        every { translationRepository() } returns mockk(relaxUnitFun = true) {
            coEvery { isProcessed(any()) } returns false
            coEvery { addAemImports(any(), any()) } returns true
        }
    }
    private val api = mockk<AemApi>()
    private val fileManager = mockk<AemArticleManager.FileManager>()
    private val manifestManager = mockk<ManifestManager>()
    private val testScope = TestScope()

    private lateinit var articleManager: AemArticleManager

    @Before
    fun setup() {
        articleManager = spyk(
            AemArticleManager(aemDb, api, fileManager, manifestManager, testScope),
            recordPrivateCalls = true
        )
    }

    // region Translations
    @Test
    fun `processDownloadedTranslations()`() = testScope.runTest {
        val translation = Translation()
        val translations = listOf(translation)
        val imports = listOf<Uri>(mockk())
        val manifest = mockk<Manifest> { every { aemImports } returns imports }
        val repository = aemDb.translationRepository()
        coEvery { repository.isProcessed(any()) } returns false
        coEvery { manifestManager.getManifest(translation) } returns manifest
        coEvery { articleManager.syncAemImportsFromManifest(any(), any()) } returns Unit

        articleManager.processDownloadedTranslations(translations)
        testScheduler.advanceUntilIdle()
        coVerifyAll {
            repository.isProcessed(translation)
            manifestManager.getManifest(translation)
            repository.addAemImports(translation, imports)
            repository.removeMissingTranslations(translations)
        }
        coVerify { articleManager.syncAemImportsFromManifest(manifest, any()) }
    }

    @Test
    fun `processDownloadedTranslations() - Don't process already processed translations`() = testScope.runTest {
        val translation = Translation()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository()
        coEvery { repository.isProcessed(translation) } returns true

        articleManager.processDownloadedTranslations(translations)
        coVerifyAll {
            repository.isProcessed(translation)
            repository.removeMissingTranslations(translations)
            manifestManager wasNot Called
        }
        coVerify(inverse = true) { articleManager.syncAemImportsFromManifest(any(), any()) }
    }

    @Test
    fun `processDownloadedTranslations() - Don't process without a downloaded manifest`() = testScope.runTest {
        val translation = Translation()
        val translations = listOf(translation)
        val repository = aemDb.translationRepository()
        coEvery { repository.isProcessed(translation) } returns false
        coEvery { manifestManager.getManifest(translation) } returns null

        articleManager.processDownloadedTranslations(translations)
        coVerifyAll {
            repository.isProcessed(translation)
            manifestManager.getManifest(translation)
            repository.removeMissingTranslations(translations)
        }
        coVerify(inverse = true) { articleManager.syncAemImportsFromManifest(any(), any()) }
    }
    // endregion Translations

    // region Download Resource
    @Test
    fun testDownloadResource() = testScope.runTest {
        val uri = mockk<Uri>()
        val resource = Resource(uri)
        val response = ByteArray(0).toResponseBody()
        coEvery { resourceDao.find(uri) } returns resource
        coEvery { api.downloadResource(uri) } returns Response.success(response)
        coEvery { fileManager.storeResponse(any(), any()) } returns null

        articleManager.downloadResource(uri, false)
        coVerifyAll {
            resourceDao.find(uri)
            api.downloadResource(uri)
            fileManager.storeResponse(response, resource)
        }
    }

    @Test
    fun `Don't download resource if it doesn't exist in the database`() = testScope.runTest {
        val uri = mockk<Uri>()
        articleManager.downloadResource(uri, false)
        coVerifyAll {
            resourceDao.find(uri)
            api wasNot Called
        }
    }

    @Test
    fun `Don't download resource if it has already been downloaded`() = testScope.runTest {
        val uri = mockk<Uri>()
        val resource = mockk<Resource> { every { needsDownload() } returns false }
        coEvery { resourceDao.find(uri) } returns resource

        articleManager.downloadResource(uri, false)
        coVerifyAll {
            resourceDao.find(uri)
            resource.needsDownload()
            api wasNot Called
        }
    }
    // endregion Download Resource

    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, 15234L.roundTimestamp(1000))
    }
}
