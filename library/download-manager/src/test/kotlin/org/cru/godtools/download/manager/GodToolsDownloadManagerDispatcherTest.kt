package org.cru.godtools.download.manager

import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.junit.Test
import org.keynote.godtools.android.db.GodToolsDao

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsDownloadManagerDispatcherTest {
    private val pinnedTranslationsChannel = Channel<List<Translation>>()

    private val dao = mockk<GodToolsDao> {
        every { getAsFlow(QUERY_PINNED_TRANSLATIONS) } returns pinnedTranslationsChannel.consumeAsFlow()
    }
    private val downloadManager = mockk<GodToolsDownloadManager> {
        coEvery { downloadLatestPublishedTranslation(any()) } returns true
    }

    @Test
    fun verifyPinnedTranslationsJobBehavior() = runTest {
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

    private suspend inline fun TestScope.testDispatcher(block: () -> Unit) {
        val dispatcher = GodToolsDownloadManager.Dispatcher(dao, downloadManager, this)
        block()
        dispatcher.shutdown()
    }
}
