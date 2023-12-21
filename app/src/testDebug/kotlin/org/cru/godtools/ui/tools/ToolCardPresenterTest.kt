package org.cru.godtools.ui.tools

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.presenterTestOf
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.io.File
import java.util.Locale
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
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
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
    private val toolFlow = MutableStateFlow(randomTool(TOOL, bannerId = BANNER_ID))
    private val bannerFlow = MutableSharedFlow<Attachment?>(extraBufferCapacity = 1)
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val enTranslationFlow = MutableSharedFlow<Translation?>(extraBufferCapacity = 1)
    private val frTranslationFlow = MutableSharedFlow<Translation?>(extraBufferCapacity = 1)

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { findAttachmentFlow(any()) } returns flowOf(null)
        every { findAttachmentFlow(BANNER_ID) } returns bannerFlow
    }
    private val fileSystem: ToolFileSystem = mockk()
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@ToolCardPresenterTest.appLanguageFlow
        every { appLanguage } returns this@ToolCardPresenterTest.appLanguageFlow.value
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { findLatestTranslationFlow(TOOL, any()) } returns flowOf(null)
        every { findLatestTranslationFlow(TOOL, Locale.ENGLISH) } returns enTranslationFlow
        every { findLatestTranslationFlow(TOOL, Locale.FRENCH) } returns frTranslationFlow
    }

    private val presenter = ToolCardPresenter(
        fileSystem = fileSystem,
        settings = settings,
        attachmentsRepository = attachmentsRepository,
        translationsRepository = translationsRepository,
    )

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
        appLanguageFlow.value = Locale.FRENCH
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
        appLanguageFlow.value = Locale.FRENCH
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
        appLanguageFlow.value = Locale.FRENCH
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
    // endregion ToolCard.State.secondLanguage

    // region ToolCard.State.secondTranslation
    @Test
    fun `ToolCardState - secondTranslation`() = runTest {
        toolFlow.value = randomTool(TOOL)
        val language = Language(Locale.FRENCH)
        val translation = randomTranslation(TOOL, Locale.FRENCH)

        presenterTestOf(
            presentFunction = { presenter.present(tool = toolFlow.collectAsState().value, secondLanguage = language) }
        ) {
            frTranslationFlow.emit(translation)
            assertEquals(translation, expectMostRecentItem().secondTranslation)
        }
    }

    @Test
    fun `ToolCardState - secondTranslation - Doesn't match the language for the main translation`() = runTest {
        toolFlow.value = randomTool(TOOL)
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

            assertNotNull(expectMostRecentItem()) { state ->
                assertNull(state.secondTranslation)
                assertEquals(translation, state.translation)
            }
        }
    }
    // endregion ToolCard.State.secondTranslation
}
