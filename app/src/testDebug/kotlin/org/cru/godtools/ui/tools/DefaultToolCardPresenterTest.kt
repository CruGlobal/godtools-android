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
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.UserCounter
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.shared.user.activity.UserCounterNames.LESSON_COMPLETION
import org.cru.godtools.ui.tools.ToolCardPresenter.UiEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "tool"
private const val BANNER_ID = 1L

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DefaultToolCardPresenterTest {
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
    private val userCountersRepository: UserCountersRepository = mockk {
        every { findCounterFlow(any()) } returns flowOf(null)
    }
    private val events = TestEventSink<UiEvent>()

    private val presenter = DefaultToolCardPresenter(
        fileSystem = fileSystem,
        settings = settings,
        attachmentsRepository = attachmentsRepository,
        languagesRepository = languagesRepository,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
        userCountersRepository = userCountersRepository,
    )

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    // region UiState.tool
    @Test
    fun `UiState - tool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertEquals(toolFlow.value, expectMostRecentItem().tool)
        }
    }

    @Test
    fun `UiState - tool - emit new state on update`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertEquals(toolFlow.value, expectMostRecentItem().tool)

            toolFlow.value = randomTool(TOOL)
            assertEquals(toolFlow.value, expectMostRecentItem().tool)
        }
    }
    // endregion UiState.tool

    // region UiState.banner
    @Test
    fun `UiState - banner`() = runTest {
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
    fun `UiState - banner - don't return banners not downloaded yet`() = runTest {
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
    fun `UiState - banner - emit new state on Attachment update`() = runTest {
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
    // endregion UiState.banner

    // region UiState.isLoaded
    @Test
    fun `UiState - isLoaded`() = runTest {
        toolFlow.value = randomTool(TOOL, defaultLocale = Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertFalse(expectMostRecentItem().isLoaded)

            // emit translation for default language
            frTranslationFlow.emit(randomTranslation(TOOL, Locale.FRENCH))
            expectNoEvents()

            // emit translation for app language
            enTranslationFlow.emit(randomTranslation(TOOL, Locale.ENGLISH))
            assertTrue(expectMostRecentItem().isLoaded)
        }
    }
    // endregion UiState.isLoaded

    // region UiState.translation
    @Test
    fun `UiState - translation`() = runTest {
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
    fun `UiState - translation - fallback to default language`() = runTest {
        toolFlow.value = randomTool(TOOL, defaultLocale = Locale.ENGLISH)
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
    fun `UiState - translation - don't emit fallback if primary hasn't loaded yet`() = runTest {
        toolFlow.value = randomTool(TOOL, defaultLocale = Locale.ENGLISH)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.ENGLISH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            enTranslationFlow.emit(translation)

            assertNull(expectMostRecentItem().translation)
        }
    }

    @Test
    fun `UiState - translation - custom locale`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.ENGLISH
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = {
                presenter.present(tool = toolFlow.collectAsState().value, customLocale = Locale.FRENCH)
            }
        ) {
            frTranslationFlow.emit(translation)

            val state = expectMostRecentItem()
            assertTrue(state.isLoaded)
            assertEquals(translation, state.translation)
        }
    }

    @Test
    fun `UiState - translation - custom locale - fallback to default language`() = runTest {
        toolFlow.value = randomTool(TOOL, defaultLocale = Locale.ENGLISH)
        appLocaleState.value = Locale.GERMAN
        val translation = randomTranslation(TOOL, Locale.ENGLISH)

        presenterTestOf(
            presentFunction = {
                presenter.present(tool = toolFlow.collectAsState().value, customLocale = Locale.FRENCH)
            }
        ) {
            enTranslationFlow.emit(translation)
            assertNotNull(expectMostRecentItem()) {
                assertNull(
                    it.translation,
                    "Translation should not be returned until the custom translation has attempted to load"
                )
                assertFalse(it.isLoaded, "isLoaded should be false until the custom translation has attempted to load")
            }

            frTranslationFlow.emit(null)
            assertNotNull(expectMostRecentItem()) {
                assertTrue(it.isLoaded)
                assertEquals(translation, it.translation)
            }
        }
    }
    // endregion UiState.translation

    // region UiState.appLanguage
    @Test
    fun `UiState - appLanguage`() = runTest {
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
    fun `UiState - appLanguage - loadAppLanguage=false`() = runTest {
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
    // endregion UiState.appLanguage

    // region UiState.appLanguageAvailable
    @Test
    fun `UiState - appLanguageAvailable`() = runTest {
        toolFlow.value = randomTool(TOOL)
        appLocaleState.value = Locale.FRENCH
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value) }
        ) {
            assertFalse(expectMostRecentItem().appLanguageAvailable)
            frTranslationFlow.emit(translation)

            assertTrue(expectMostRecentItem().appLanguageAvailable)
        }
    }
    // endregion UiState.appLanguageAvailable

    // region UiState.secondLanguage
    @Test
    fun `UiState - secondLanguage`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val language = Language(Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, secondLanguage = language) }
        ) {
            assertEquals(language, expectMostRecentItem().secondLanguage)
        }
    }
    // endregion UiState.secondLanguage

    // region UiState.secondLanguageAvailable
    @Test
    fun `UiState - secondLanguageAvailable`() = runTest {
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
    // endregion UiState.secondLanguageAvailable

    // region UiState.progress
    @Test
    fun `UiState - progress - not started`() = runTest {
        val tool = randomTool(TOOL, Tool.Type.LESSON, progress = null)

        presenterTestOf(presentFunction = { presenter.present(tool) }) {
            assertNull(expectMostRecentItem().progress)
        }
    }

    @Test
    fun `UiState - progress - in progress`() = runTest {
        val tool = randomTool(TOOL, Tool.Type.LESSON, progress = Random.nextDouble(0.0, 1.0))

        presenterTestOf(presentFunction = { presenter.present(tool) }) {
            assertEquals(
                tool.progress!!,
                assertIs<UiState.Progress.InProgress>(expectMostRecentItem().progress).progress,
                0.0001
            )
        }
    }

    @Test
    fun `UiState - progress - completed`() = runTest {
        val tool = randomTool(TOOL, Tool.Type.LESSON, progress = Random.nextDouble(0.0, 1.0))
        every {
            userCountersRepository.findCounterFlow(LESSON_COMPLETION(TOOL))
        } returns flowOf(UserCounter(apiCount = 1))

        presenterTestOf(presentFunction = { presenter.present(tool) }) {
            assertEquals(UiState.Progress.Completed, expectMostRecentItem().progress)
        }
    }
    // endregion UiState.progress

    // region UiState.availableLanguages
    @Test
    fun `UiState - availableLanguages`() = runTest {
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
    fun `UiState - availableLanguages - loadAvailableLanguages=false`() = runTest {
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
    fun `UiState - availableLanguages - Only distinct languages are counted`() = runTest {
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
    // endregion UiState.availableLanguages

    // region UiState
    @Test
    fun `UiState - GT-2364 - App Language Not Available, Second language matches Default language`() = runTest {
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
                assertFalse(state.appLanguageAvailable)
                assertEquals(Language(Locale.ENGLISH), state.secondLanguage)
                assertTrue(state.secondLanguageAvailable)
            }
        }
    }
    // endregion UiState

    // region UiEvent.Click
    @Test
    fun `UiEvent - Click`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(UiEvent.Click)
        }

        events.assertEvent(UiEvent.Click)
    }
    // endregion UiEvent.Click

    // region UiEvent.OpenTool
    @Test
    fun `UiEvent - OpenTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(UiEvent.OpenTool)
        }

        events.assertEvent(UiEvent.OpenTool)
    }
    // endregion UiEvent.OpenTool

    // region UiEvent.OpenToolDetails
    @Test
    fun `UiEvent - OpenToolDetails`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(UiEvent.OpenToolDetails)
        }

        events.assertEvent(UiEvent.OpenToolDetails)
    }
    // endregion UiEvent.OpenToolDetails

    // region UiEvent.PinTool
    @Test
    fun `UiEvent - PinTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(UiEvent.PinTool)
        }

        coVerifyAll { toolsRepository.pinTool(TOOL) }
        events.assertNoEvents()
    }
    // endregion UiEvent.PinTool

    // region UiEvent.UnpinTool
    @Test
    fun `UiEvent - UnpinTool`() = runTest {
        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, eventSink = events) }
        ) {
            expectMostRecentItem().eventSink(UiEvent.UnpinTool)
        }

        coVerifyAll { toolsRepository.unpinTool(TOOL) }
        events.assertNoEvents()
    }
    // endregion UiEvent.UnpinTool
}
