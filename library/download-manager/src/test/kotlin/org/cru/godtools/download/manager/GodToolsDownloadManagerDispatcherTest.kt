package org.cru.godtools.download.manager

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.DownloadedFilesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.DownloadedFile
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsDownloadManagerDispatcherTest {
    private val appLanguageFlow = MutableSharedFlow<Locale>(replay = 1)
    private val primaryLanguageFlow = MutableSharedFlow<Locale>(replay = 1)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(replay = 1)
    private val attachmentsFlow = MutableSharedFlow<List<Attachment>>(replay = 1)
    private val downloadedFilesFlow = MutableSharedFlow<List<DownloadedFile>>(replay = 1)
    private val favoriteToolsFlow = MutableSharedFlow<List<Tool>>(replay = 1)
    private val resourcesFlow = MutableSharedFlow<List<Tool>>(replay = 1)

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { getAttachmentsFlow() } returns attachmentsFlow
    }
    private val downloadManager = mockk<GodToolsDownloadManager> {
        coEvery { downloadLatestPublishedTranslation(any()) } returns true
        coJustRun { downloadAttachment(any()) }
    }
    private val downloadedFilesRepository: DownloadedFilesRepository = mockk {
        every { getDownloadedFilesFlow() } returns downloadedFilesFlow
    }
    private val settings = mockk<Settings> {
        every { appLanguageFlow } returns this@GodToolsDownloadManagerDispatcherTest.appLanguageFlow
        every { primaryLanguageFlow } returns this@GodToolsDownloadManagerDispatcherTest.primaryLanguageFlow
        every { parallelLanguageFlow } returns this@GodToolsDownloadManagerDispatcherTest.parallelLanguageFlow
    }
    private val toolsRepository: ToolsRepository by lazy {
        mockk {
            every { getResourcesFlow() } returns resourcesFlow
            every { getFavoriteToolsFlow() } returns favoriteToolsFlow
        }
    }
    private val translationsRepository: TranslationsRepository by lazy {
        mockk {
            every { getTranslationsForToolsAndLocalesFlow(any(), any()) } returns flowOf(emptyList())
        }
    }
    private val testScope = TestScope()

    private lateinit var dispatcher: GodToolsDownloadManager.Dispatcher

    @Before
    fun startDispatcher() {
        dispatcher = GodToolsDownloadManager.Dispatcher(
            attachmentsRepository,
            downloadManager,
            downloadedFilesRepository,
            settings,
            toolsRepository,
            translationsRepository,
            testScope.backgroundScope,
        )
    }

    @Test
    fun `Favorite Tools downloadLatestPublishedTranslation() - app language`() = testScope.runTest {
        dispatcher.downloadTranslationsForDefaultLanguageJob.cancel()

        val translationsFlow = MutableSharedFlow<List<Translation>>(replay = 1)
        every {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Locale.FRENCH) }
            )
        } returns translationsFlow
        verify { downloadManager wasNot Called }

        favoriteToolsFlow.emit(listOf(Tool("tool1"), Tool("tool2")))
        appLanguageFlow.emit(Locale.FRENCH)
        runCurrent()
        verifyAll {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Locale.FRENCH) }
            )
        }

        val translation1 = Translation("tool1", Locale.FRENCH)
        val translation2 = Translation("tool2", Locale.FRENCH)
        translationsFlow.emit(listOf(translation1, translation2))
        runCurrent()
        coVerifyAll {
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool1", Locale.FRENCH))
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool2", Locale.FRENCH))
        }
    }

    @Test
    fun `Favorite Tools downloadLatestPublishedTranslation() - default language`() = testScope.runTest {
        val translationsFlow = MutableSharedFlow<List<Translation>>(replay = 1)
        every {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Settings.defaultLanguage) }
            )
        } returns translationsFlow
        verify { downloadManager wasNot Called }

        favoriteToolsFlow.emit(listOf(Tool("tool1"), Tool("tool2")))
        runCurrent()
        verifyAll {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Settings.defaultLanguage) }
            )
        }

        val translation1 = Translation("tool1", Settings.defaultLanguage)
        val translation2 = Translation("tool2", Settings.defaultLanguage)
        translationsFlow.emit(listOf(translation1, translation2))
        runCurrent()
        coVerifyAll {
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool1", Settings.defaultLanguage))
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool2", Settings.defaultLanguage))
        }
    }

    @Test
    fun `Favorite Tools downloadLatestPublishedTranslation() - primary & parallel language`() = testScope.runTest {
        dispatcher.downloadTranslationsForDefaultLanguageJob.cancel()

        val translationsFlow = MutableSharedFlow<List<Translation>>(replay = 1)
        every {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Settings.defaultLanguage, Locale.FRENCH, Locale.GERMAN) }
            )
        } returns translationsFlow
        verify { downloadManager wasNot Called }

        favoriteToolsFlow.emit(listOf(Tool("tool1"), Tool("tool2")))
        primaryLanguageFlow.emit(Locale.GERMAN)
        parallelLanguageFlow.emit(Locale.FRENCH)
        runCurrent()
        verifyAll {
            translationsRepository.getTranslationsForToolsAndLocalesFlow(
                tools = match { it.toSet() == setOf("tool1", "tool2") },
                locales = match { it.toSet() == setOf(Settings.defaultLanguage, Locale.FRENCH, Locale.GERMAN) }
            )
        }

        val translation1 = Translation().apply {
            toolCode = "tool1"
            languageCode = Locale.ENGLISH
        }
        val translation2 = Translation().apply {
            toolCode = "tool2"
            languageCode = Locale.FRENCH
        }
        translationsFlow.emit(listOf(translation1, translation2))
        runCurrent()
        coVerifyAll {
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool1", Locale.ENGLISH))
            downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool2", Locale.FRENCH))
        }
    }

    @Test
    fun `staleAttachmentsJob should download any downloaded attachments missing the local file`() = testScope.runTest {
        verify { downloadManager wasNot Called }
        val attachment1 = Attachment().apply {
            id = Random.nextLong()
            sha256 = "sha1"
            isDownloaded = true
        }
        val attachment2 = Attachment().apply {
            id = Random.nextLong()
            sha256 = "sha2"
            isDownloaded = true
        }
        val attachment3 = Attachment().apply {
            id = Random.nextLong()
            sha256 = "sha3"
            isDownloaded = false
        }
        attachmentsFlow.emit(listOf(attachment1, attachment2, attachment3))
        downloadedFilesFlow.emit(listOf(DownloadedFile("sha2.bin")))
        runCurrent()
        coVerify(exactly = 1) {
            downloadManager.downloadAttachment(attachment1.id)
        }
        confirmVerified(downloadManager)
    }

    @Test
    fun `toolBannerAttachmentsJob should download any banner attachments`() = testScope.runTest {
        verify { downloadManager wasNot Called }

        val attachments = List(7) { i ->
            Attachment().apply {
                id = Random.nextLong()
                isDownloaded = i < 3
            }
        }
        val tool1 = Tool().apply {
            bannerId = attachments[0].id
            detailsBannerId = attachments[1].id
            detailsBannerAnimationId = attachments[2].id
        }
        val tool2 = Tool().apply {
            bannerId = attachments[3].id
            detailsBannerId = attachments[4].id
            detailsBannerAnimationId = attachments[5].id
        }

        attachmentsFlow.emit(attachments)
        resourcesFlow.emit(listOf(tool1, tool2))
        runCurrent()
        coVerify(exactly = 1) {
            downloadManager.downloadAttachment(attachments[3].id)
            downloadManager.downloadAttachment(attachments[4].id)
            downloadManager.downloadAttachment(attachments[5].id)
        }
        confirmVerified(downloadManager)
    }
}
