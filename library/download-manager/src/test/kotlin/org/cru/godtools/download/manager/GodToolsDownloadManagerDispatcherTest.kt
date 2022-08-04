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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.db.DownloadManagerRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsDownloadManagerDispatcherTest {
    private val primaryLanguageFlow = MutableSharedFlow<Locale>(replay = 1)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(replay = 1)
    private val favoritedTranslationsFlow = MutableSharedFlow<List<Translation>>()
    private val staleAttachmentsChannel = Channel<List<Attachment>>()
    private val toolBannerAttachmentsChannel = Channel<List<Attachment>>()

    private val dao = mockk<GodToolsDao> {
        every { getAsFlow(QUERY_STALE_ATTACHMENTS) } returns staleAttachmentsChannel.consumeAsFlow()
        every { getAsFlow(QUERY_TOOL_BANNER_ATTACHMENTS) } returns toolBannerAttachmentsChannel.consumeAsFlow()
    }
    private val downloadManager = mockk<GodToolsDownloadManager> {
        coEvery { downloadLatestPublishedTranslation(any()) } returns true
        coJustRun { downloadAttachment(any()) }
    }
    private val repository = mockk<DownloadManagerRepository> {
        every { getFavoriteTranslationsThatNeedDownload(any()) } returns favoritedTranslationsFlow
    }
    private val settings = mockk<Settings> {
        every { primaryLanguageFlow } returns this@GodToolsDownloadManagerDispatcherTest.primaryLanguageFlow
        every { parallelLanguageFlow } returns this@GodToolsDownloadManagerDispatcherTest.parallelLanguageFlow
    }

    @Test
    fun `favoriteToolsJob should trigger downloadLatestPublishedTranslation()`() = runTest {
        testDispatcher {
            verify {
                downloadManager wasNot Called
                repository wasNot Called
            }

            val translation1 = Translation().apply {
                toolCode = "tool1"
                languageCode = Locale.ENGLISH
            }
            val translation2 = Translation().apply {
                toolCode = "tool2"
                languageCode = Locale.FRENCH
            }
            primaryLanguageFlow.emit(Locale.GERMAN)
            parallelLanguageFlow.emit(Locale.FRENCH)
            runCurrent()
            verifyAll {
                repository.getFavoriteTranslationsThatNeedDownload(
                    match { it.toSet() == setOf(Settings.defaultLanguage, Locale.FRENCH, Locale.GERMAN) }
                )
            }
            favoritedTranslationsFlow.emit(listOf(translation1, translation2))
            runCurrent()
            coVerifyAll {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool1", Locale.ENGLISH))
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool2", Locale.FRENCH))
            }
        }
    }

    @Test
    fun `staleAttachmentsJob should download any banner attachments`() = runTest {
        testDispatcher {
            verify { downloadManager wasNot Called }

            val attachment1 = Attachment().apply { id = Random.nextLong() }
            val attachment2 = Attachment().apply { id = Random.nextLong() }
            staleAttachmentsChannel.send(listOf(attachment1, attachment2))
            runCurrent()
            coVerify(exactly = 1) {
                downloadManager.downloadAttachment(attachment1.id)
                downloadManager.downloadAttachment(attachment2.id)
            }
            confirmVerified(downloadManager)
        }
    }

    @Test
    fun `toolBannerAttachmentsJob should download any banner attachments`() = runTest {
        testDispatcher {
            verify { downloadManager wasNot Called }

            val attachment1 = Attachment().apply { id = Random.nextLong() }
            val attachment2 = Attachment().apply { id = Random.nextLong() }
            toolBannerAttachmentsChannel.send(listOf(attachment1, attachment2))
            runCurrent()
            coVerify(exactly = 1) {
                downloadManager.downloadAttachment(attachment1.id)
                downloadManager.downloadAttachment(attachment2.id)
            }
            confirmVerified(downloadManager)
        }
    }

    private suspend inline fun TestScope.testDispatcher(block: () -> Unit) {
        val dispatcher = GodToolsDownloadManager.Dispatcher(dao, downloadManager, repository, settings, this)
        block()
        dispatcher.shutdown()
    }
}
