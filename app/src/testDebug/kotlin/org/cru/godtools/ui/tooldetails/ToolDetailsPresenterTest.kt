package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import java.util.Locale
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.downloadmanager.DownloadProgress
import org.cru.godtools.downloadmanager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.cru.godtools.shortcuts.PendingShortcut
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.Event
import org.cru.godtools.ui.tools.FakeToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "tool"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsPresenterTest {
    private val appLocaleFlow = MutableStateFlow(Locale.ENGLISH)
    private val toolFlow = MutableStateFlow<Tool?>(null)
    private val normalToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { findAttachmentFlow(any()) } returns flowOf(null)
    }
    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlowForLocales(any()) } returns flowOf(emptyList())
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { findToolFlow(any()) } returns flowOf(null)
        every { findToolFlow(TOOL) } returns toolFlow
        every { getNormalToolsFlow() } returns normalToolsFlow
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { findLatestTranslationFlow(any(), any(), any()) } returns flowOf(null)
        every { getTranslationsFlowForTool(any()) } returns flowOf(emptyList())
    }

    private val downloadManager: GodToolsDownloadManager = mockk {
        every { getDownloadProgressFlow(any(), any()) } returns flowOf(null)
    }
    private val fileSystem: ToolFileSystem = mockk()
    private val manifestManager: ManifestManager = mockk {
        coEvery { getManifest(any()) } returns null
    }
    private val navigator = FakeNavigator(ToolDetailsScreen(TOOL))
    private val settings: Settings = mockk {
        every { appLanguage } returns appLocaleFlow.value
        every { appLanguageFlow } returns appLocaleFlow
    }
    private val shortcutManager: GodToolsShortcutManager = mockk {
        every { canPinToolShortcut(any()) } returns false
    }
    private val syncService: GodToolsSyncService = mockk()

    private fun createPresenter(screen: ToolDetailsScreen = ToolDetailsScreen(TOOL)) = ToolDetailsPresenter(
        context = ApplicationProvider.getApplicationContext(),
        attachmentsRepository = attachmentsRepository,
        languagesRepository = languagesRepository,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
        downloadManager = downloadManager,
        fileSystem = fileSystem,
        manifestManager = manifestManager,
        settings = settings,
        shortcutManager = shortcutManager,
        syncService = syncService,
        toolCardPresenter = FakeToolCardPresenter(),
        screen = screen,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    // region State.tool
    @Test
    fun `State - tool`() = runTest {
        createPresenter().test {
            assertNull(expectMostRecentItem().tool)

            val tool = randomTool(TOOL)
            toolFlow.value = tool
            assertEquals(tool, expectMostRecentItem().tool)
        }
    }
    // endregion State.tool

    // region State.banner
    @Test
    fun `State - banner - prefer detailsBannerId`() = runTest {
        toolFlow.value = randomTool(TOOL, detailsBannerId = Random.nextLong(), bannerId = Random.nextLong())
        val file = File.createTempFile("prefix", "suffix")
        val attachment = Attachment {
            isDownloaded = true
            filename = "file.ext"
            sha256 = "file"
        }

        every { attachmentsRepository.findAttachmentFlow(toolFlow.value?.detailsBannerId!!) } returns flowOf(attachment)
        coEvery { attachment.getFile(fileSystem) } returns file

        createPresenter().test {
            assertEquals(file, expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - fallback to bannerId`() = runTest {
        toolFlow.value = randomTool(TOOL, detailsBannerId = null, bannerId = Random.nextLong())
        val file = File.createTempFile("prefix", "suffix")
        val attachment = Attachment {
            isDownloaded = true
            filename = "file.ext"
            sha256 = "file"
        }

        every { attachmentsRepository.findAttachmentFlow(toolFlow.value?.bannerId!!) } returns flowOf(attachment)
        coEvery { attachment.getFile(fileSystem) } returns file

        createPresenter().test {
            assertEquals(file, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.bannerAnimation
    @Test
    fun `State - bannerAnimation`() = runTest {
        toolFlow.value = randomTool(TOOL, detailsBannerAnimationId = Random.nextLong())
        val file = File.createTempFile("prefix", "suffix")
        val attachment = Attachment {
            isDownloaded = true
            filename = "file.ext"
            sha256 = "file"
        }

        every {
            attachmentsRepository.findAttachmentFlow(toolFlow.value?.detailsBannerAnimationId!!)
        } returns flowOf(attachment)
        coEvery { attachment.getFile(fileSystem) } returns file

        createPresenter().test {
            assertEquals(file, expectMostRecentItem().bannerAnimation)
        }
    }
    // endregion State.bannerAnimation

    // region State.downloadProgress
    @Test
    fun `State - downloadProgress`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val translationFlow = MutableStateFlow(randomTranslation(TOOL, Locale.ENGLISH))
        val downloadProgressFlow = MutableStateFlow<DownloadProgress?>(null)

        every { translationsRepository.findLatestTranslationFlow(TOOL, Locale.ENGLISH) } returns translationFlow
        every { downloadManager.getDownloadProgressFlow(TOOL, Locale.ENGLISH) } returns downloadProgressFlow

        createPresenter().test {
            assertNull(expectMostRecentItem().downloadProgress)

            downloadProgressFlow.value = DownloadProgress(0, 1)
            assertEquals(DownloadProgress(0, 1), expectMostRecentItem().downloadProgress)
        }
    }
    // endregion State.downloadProgress

    // region State.hasShortcut
    @Test
    fun `State - hasShortcut`() = runTest {
        toolFlow.value = randomTool(TOOL)

        every { shortcutManager.canPinToolShortcut(any()) } returns true

        createPresenter().test {
            assertTrue(expectMostRecentItem().hasShortcut)
        }
    }

    @Test
    fun `State - hasShortcut - false`() = runTest {
        toolFlow.value = randomTool(TOOL)

        every { shortcutManager.canPinToolShortcut(any()) } returns false

        createPresenter().test {
            assertFalse(expectMostRecentItem().hasShortcut)
        }
    }
    // endregion State.hasShortcut

    // region Event.PinShortcut
    @Test
    fun `Event - PinShortcut`() = runTest {
        val pendingShortcut: PendingShortcut = mockk()
        every { shortcutManager.canPinToolShortcut(any()) } returns true
        every { shortcutManager.getPendingToolShortcut(TOOL) } returns pendingShortcut
        every { shortcutManager.pinShortcut(pendingShortcut) } just Runs

        createPresenter().test {
            expectMostRecentItem().eventSink(Event.PinShortcut)
        }

        verify {
            shortcutManager.pinShortcut(pendingShortcut)
        }
    }
    // endregion Event.PinShortcut

    // region Event.PinTool
    @Test
    fun `Event - PinTool`() = runTest {
        coEvery { toolsRepository.pinTool(any(), any()) } just Runs
        every { settings.setFeatureDiscovered(any()) } just Runs
        coEvery { syncService.syncDirtyFavoriteTools() } returns true

        createPresenter().test {
            expectMostRecentItem().eventSink(Event.PinTool)
        }

        coVerify {
            toolsRepository.pinTool(TOOL)
            settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
            syncService.syncDirtyFavoriteTools()
        }
    }
    // endregion Event.PinTool

    // region Event.UnpinTool
    @Test
    fun `Event - UnpinTool`() = runTest {
        coEvery { toolsRepository.unpinTool(any()) } just Runs
        coEvery { syncService.syncDirtyFavoriteTools() } returns true

        createPresenter().test {
            expectMostRecentItem().eventSink(Event.UnpinTool)
        }

        coVerify {
            toolsRepository.unpinTool(TOOL)
            syncService.syncDirtyFavoriteTools()
        }
        coVerify(exactly = 0) { settings.setFeatureDiscovered(any()) }
    }
    // endregion Event.UnpinTool

    // region Event.SwitchVariant
    @Test
    fun `Event - SwitchVariant`() = runTest {
        createPresenter(ToolDetailsScreen("initial")).test {
            assertNotNull(expectMostRecentItem()) {
                assertEquals("initial", it.toolCode)

                it.eventSink(Event.SwitchVariant("new"))
            }

            assertEquals("new", expectMostRecentItem().toolCode)
        }
    }
    // endregion Event.SwitchVariant
}
