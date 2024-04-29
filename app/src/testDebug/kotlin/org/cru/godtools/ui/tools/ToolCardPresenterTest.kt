package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.TestEventSink
import com.slack.circuit.test.presenterTestOf
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.io.File
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "tool"
private const val BANNER_ID = 1L

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolCardPresenterTest {
    private val appLocaleState = mutableStateOf(Locale.ENGLISH)
    private val toolFlow = MutableStateFlow(randomTool(TOOL, bannerId = BANNER_ID))
    private val bannerFlow = MutableSharedFlow<Attachment?>(extraBufferCapacity = 1)
    private val frLanguageFlow = MutableSharedFlow<Language?>(extraBufferCapacity = 1)
    private val enTranslationFlow = MutableSharedFlow<Translation?>(extraBufferCapacity = 1)
    private val frTranslationFlow = MutableSharedFlow<Translation?>(extraBufferCapacity = 1)

    private val fileSystem: ToolFileSystem = mockk()
    private val settings: Settings = mockk {
        everyComposable { produceAppLocaleState() } returns appLocaleState
    }

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { findAttachmentFlow(any()) } returns flowOf(null)
        every { findAttachmentFlow(BANNER_ID) } returns bannerFlow
    }
    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } returns flowOf(null)
        every { findLanguageFlow(Locale.FRENCH) } returns frLanguageFlow
    }
    private val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true)
    private val translationsRepository: TranslationsRepository = mockk {
        every { findLatestTranslationFlow(TOOL, any()) } returns flowOf(null)
        every { findLatestTranslationFlow(TOOL, Locale.ENGLISH) } returns enTranslationFlow
        every { findLatestTranslationFlow(TOOL, Locale.FRENCH) } returns frTranslationFlow
    }
    private val events = TestEventSink<ToolCard.Event>()

    private val presenter = ToolCardPresenter(
        fileSystem = fileSystem,
        settings = settings,
        attachmentsRepository = attachmentsRepository,
        languagesRepository = languagesRepository,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
    )

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    // region ToolCard.State.tool
    @Test
    fun `ToolCardState - tool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertEquals(toolFlow.value, expectMostRecentItem().tool)
        }
    }

    @Test
    fun `ToolCardState - tool - emit new state on update`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertEquals(toolFlow.value, expectMostRecentItem().tool)

            toolFlow.value = randomTool(TOOL)
            assertEquals(toolFlow.value, expectMostRecentItem().tool)
        }
    }
    // endregion ToolCard.State.tool

    // region ToolCard.State.banner
    @Test
    fun `ToolCardState - banner`() = runTest {
        val banner = Attachment(BANNER_ID) {
            sha256 = "0123456789abcdef"
            isDownloaded = true
        }

        val file = File.createTempFile("tmp", null)
        coEvery { banner.getFile(fileSystem) } returns file

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            bannerFlow.emit(banner)
            assertEquals(file, expectMostRecentItem().banner)
        }
    }

    @Test
    fun `ToolCardState - banner - don't return banners not downloaded yet`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            bannerFlow.emit(
                Attachment(BANNER_ID) {
                    sha256 = "0123456789abcdef"
                    isDownloaded = false
                }
            )
            assertNull(expectMostRecentItem().banner)
        }

        verifyAll {
            attachmentsRepository.findAttachmentFlow(BANNER_ID)
            fileSystem wasNot Called
        }
    }

    @Test
    fun `ToolCardState - banner - emit new state on Attachment update`() = runTest {
        val banner = Attachment(BANNER_ID) {
            sha256 = "0123456789abcdef"
            isDownloaded = true
        }

        val file = File.createTempFile("tmp", null)
        coEvery { banner.getFile(fileSystem) } returns file

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            bannerFlow.emit(Attachment(BANNER_ID) { isDownloaded = false })
            assertNull(expectMostRecentItem().banner)

            bannerFlow.emit(banner)
            assertEquals(file, expectMostRecentItem().banner)
        }
    }
    // endregion ToolCard.State.banner

    // region ToolCard.State.translation
    @Test
    fun `ToolCardState - translation`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            frTranslationFlow.emit(translation)

            val state = expectMostRecentItem()
            assertTrue(state.isLoaded)
            assertEquals(translation, state.translation)
        }
    }

    @Test
    fun `ToolCardState - translation - fallback to default language`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.ENGLISH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            frTranslationFlow.emit(null)
            enTranslationFlow.emit(translation)

            val state = expectMostRecentItem()
            assertTrue(state.isLoaded)
            assertEquals(translation, state.translation)
        }
    }

    @Test
    fun `ToolCardState - translation - don't emit fallback if primary hasn't loaded yet`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.ENGLISH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            enTranslationFlow.emit(translation)

            val state = expectMostRecentItem()
            assertFalse(state.isLoaded, "isLoaded should only be true once the translation flow emits a value")
            assertNull(state.translation)
        }
    }
    // endregion ToolCard.State.translation

    // region ToolCard.State.appLanguage
    @Test
    fun `ToolCardState - appLanguage`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, loadAppLanguage = true) }
        ) {
            frLanguageFlow.emit(Language(Locale.FRENCH))
            assertEquals(Language(Locale.FRENCH), expectMostRecentItem().appLanguage)
        }
    }

    @Test
    fun `ToolCardState - appLanguage - loadAppLanguage=false`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, loadAppLanguage = false) }
        ) {
            frLanguageFlow.emit(Language(Locale.FRENCH))
            assertNull(expectMostRecentItem().appLanguage)
        }

        verifyAll { languagesRepository wasNot Called }
    }
    // endregion ToolCard.State.appLanguage

    // region ToolCard.State.appTranslation
    @Test
    fun `ToolCardState - appTranslation`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertNull(expectMostRecentItem().appTranslation)
            frTranslationFlow.emit(translation)

            assertEquals(translation, expectMostRecentItem().appTranslation)
        }
    }
    // endregion ToolCard.State.appTranslation

    // region ToolCard.State.secondLanguage
    @Test
    fun `ToolCardState - secondLanguage`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val language = Language(Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, secondLanguage = language) }
        ) {
            assertEquals(language, expectMostRecentItem().secondLanguage)
        }
    }

    @Test
    fun `ToolCardState - secondLanguage - GT-2362 Removed when matches appLanguage`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val language = Language(appLocaleState.value)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, secondLanguage = language) }
        ) {
            assertNull(expectMostRecentItem().secondLanguage)
        }
    }
    // endregion ToolCard.State.secondLanguage

    // region ToolCard.State.secondLanguageAvailable
    @Test
    fun `ToolCardState - secondLanguageAvailable`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val language = Language(Locale.FRENCH)
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, secondLanguage = language) }
        ) {
            frTranslationFlow.emit(translation)
            assertTrue(expectMostRecentItem().secondLanguageAvailable)
        }
    }
    // endregion ToolCard.State.secondLanguageAvailable

    // region ToolCard.State.availableLanguages
    @Test
    fun `ToolCardState - availableLanguages`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val translations = listOf(
            randomTranslation(languageCode = Locale.ENGLISH),
            randomTranslation(languageCode = Locale.FRENCH),
        )
        every { translationsRepository.getTranslationsFlowForTool(TOOL) } returns flowOf(translations)

        presenterTestOf(
            presentFunction = {
                presenter.present(tool = toolFlow.collectAsState().value, loadAvailableLanguages = true)
            }
        ) {
            assertEquals(2, expectMostRecentItem().availableLanguages)
        }

        verify { translationsRepository.getTranslationsFlowForTool(TOOL) }
    }

    @Test
    fun `ToolCardState - availableLanguages - loadAvailableLanguages=false`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val translations = listOf(
            randomTranslation(languageCode = Locale.ENGLISH),
            randomTranslation(languageCode = Locale.FRENCH),
        )
        every { translationsRepository.getTranslationsFlowForTool(TOOL) } returns flowOf(translations)

        presenterTestOf(
            presentFunction = {
                presenter.present(tool = toolFlow.collectAsState().value, loadAvailableLanguages = false)
            }
        ) {
            assertEquals(0, expectMostRecentItem().availableLanguages)
        }

        verify(exactly = 0) { translationsRepository.getTranslationsFlowForTool(TOOL) }
    }

    @Test
    fun `ToolCardState - availableLanguages - Only distinct languages are counted`() = runTest {
        toolFlow.value = randomTool(TOOL)
        every { translationsRepository.getTranslationsFlowForTool(TOOL) }.returns(
            flowOf(
                listOf(
                    randomTranslation(languageCode = Locale.ENGLISH),
                    randomTranslation(languageCode = Locale.ENGLISH)
                )
            )
        )

        presenterTestOf(
            presentFunction = {
                presenter.present(tool = toolFlow.collectAsState().value, loadAvailableLanguages = true)
            }
        ) {
            assertEquals(1, expectMostRecentItem().availableLanguages)
        }

        verify { translationsRepository.getTranslationsFlowForTool(TOOL) }
    }
    // endregion ToolCard.State.availableLanguages

    // region ToolCard.State
    @Test
    fun `ToolCardState - GT-2364 - App Language Not Available, Second language matches Default language`() = runTest {
        appLocaleState.value = Locale.FRENCH
        toolFlow.value = randomTool(TOOL, defaultLocale = Locale.ENGLISH)
        val translation = randomTranslation(TOOL, Locale.ENGLISH)

        presenterTestOf(
            presentFunction = {
                presenter.present(
                    tool = toolFlow.collectAsState().value,
                    secondLanguage = Language(Locale.ENGLISH),
                )
            }
        ) {
            enTranslationFlow.emit(translation)
            frTranslationFlow.emit(null)

            assertNotNull(expectMostRecentItem()) { state ->
                assertNotNull(state.translation) {
                    assertEquals(Locale.ENGLISH, it.languageCode)
                }
                assertNull(state.appTranslation)
                assertEquals(Language(Locale.ENGLISH), state.secondLanguage)
                assertTrue(state.secondLanguageAvailable)
            }
        }
    }
    // endregion ToolCard.State

    // region ToolCard.Event.Click
    @Test
    fun `ToolCardEvent - Click`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(ToolCard.Event.Click)
        }

        events.assertEvent(ToolCard.Event.Click)
    }
    // endregion ToolCard.Event.Click

    // region ToolCard.Event.OpenTool
    @Test
    fun `ToolCardEvent - OpenTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(ToolCard.Event.OpenTool)
        }

        events.assertEvent(ToolCard.Event.OpenTool)
    }
    // endregion ToolCard.Event.OpenTool

    // region ToolCard.Event.OpenToolDetails
    @Test
    fun `ToolCardEvent - OpenToolDetails`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(ToolCard.Event.OpenToolDetails)
        }

        events.assertEvent(ToolCard.Event.OpenToolDetails)
    }
    // endregion ToolCard.Event.OpenToolDetails

    // region ToolCard.Event.PinTool
    @Test
    fun `ToolCardEvent - PinTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(ToolCard.Event.PinTool)
        }

        coVerifyAll { toolsRepository.pinTool(TOOL) }
        events.assertNoEvents()
    }
    // endregion ToolCard.Event.PinTool

    // region ToolCard.Event.UnpinTool
    @Test
    fun `ToolCardEvent - UnpinTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(ToolCard.Event.UnpinTool)
        }

        coVerifyAll { toolsRepository.unpinTool(TOOL) }
        events.assertNoEvents()
    }
    // endregion ToolCard.Event.UnpinTool
}
