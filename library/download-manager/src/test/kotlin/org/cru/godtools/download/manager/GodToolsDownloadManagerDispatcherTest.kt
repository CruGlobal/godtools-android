package org.cru.godtools.download.manager

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsDownloadManagerDispatcherTest {
    private val pinnedTranslationsChannel = Channel<List<Translation>>()
    private val staleAttachmentsChannel = Channel<List<Attachment>>()
    private val toolBannerAttachmentsChannel = Channel<List<Attachment>>()

    private val dao = mockk<GodToolsDao> {
        every { getAsFlow(QUERY_PINNED_TRANSLATIONS) } returns pinnedTranslationsChannel.consumeAsFlow()
        every { getAsFlow(QUERY_STALE_ATTACHMENTS) } returns staleAttachmentsChannel.consumeAsFlow()
        every { getAsFlow(QUERY_TOOL_BANNER_ATTACHMENTS) } returns toolBannerAttachmentsChannel.consumeAsFlow()
    }
    private val downloadManager = mockk<GodToolsDownloadManager> {
        coEvery { downloadLatestPublishedTranslation(any()) } returns true
        coJustRun { downloadAttachment(any()) }
    }

    @Test
    fun `pinnedTranslationsJob should trigger downloadLatestPublishedTranslation()`() = runTest {
        testDispatcher {
            verify { downloadManager wasNot Called }

            val translation1 = Translation().apply {
                toolCode = "tool1"
                languageCode = Locale.ENGLISH
            }
            val translation2 = Translation().apply {
                toolCode = "tool2"
                languageCode = Locale.FRENCH
            }
            pinnedTranslationsChannel.send(listOf(translation1, translation2))
            runCurrent()
            coVerify(exactly = 1) {
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool1", Locale.ENGLISH))
                downloadManager.downloadLatestPublishedTranslation(TranslationKey("tool2", Locale.FRENCH))
            }
            confirmVerified(downloadManager)
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
        val dispatcher = GodToolsDownloadManager.Dispatcher(dao, downloadManager, this)
        block()
        dispatcher.shutdown()
    }
}
