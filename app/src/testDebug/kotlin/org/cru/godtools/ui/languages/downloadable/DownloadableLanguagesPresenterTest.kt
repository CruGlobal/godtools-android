package org.cru.godtools.ui.languages.downloadable

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.randomLanguage
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState.UiEvent
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadableLanguagesPresenterTest {
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val languagesFlow = MutableStateFlow(emptyList<Language>())

    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlow() } returns languagesFlow
    }
    private val navigator = FakeNavigator(DownloadableLanguagesScreen)
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@DownloadableLanguagesPresenterTest.appLanguageFlow
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { getDownloadedToolsFlowByTypesAndLanguage(any(), any()) } returns flowOf(emptyList())
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
    }

    private val presenter = DownloadableLanguagesPresenter(
        context = ApplicationProvider.getApplicationContext(),
        languagesRepository = languagesRepository,
        settings = settings,
        toolsRepository = toolsRepository,
        ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
        navigator = navigator
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()

        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
        navigator.assertGoToIsEmpty()
    }

    // region State.languages
    @Test
    fun `State - languages`() = testScope.runTest {
        languagesFlow.value = listOf(
            randomLanguage(Locale.FRENCH, isForcedName = false, isAdded = false),
            randomLanguage(Locale.ENGLISH, isForcedName = false, isAdded = false),
            randomLanguage(Locale.GERMAN, isForcedName = false, isAdded = false)
        )

        presenter.test {
            assertEquals(
                listOf(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN),
                expectMostRecentItem().languages.map { it.language.code }
            )
        }
    }

    @Test
    fun `State - languages - downloadedTools`() = testScope.runTest {
        val toolsFlow = MutableStateFlow(
            listOf(
                randomTool("code1"),
                randomTool("code2")
            )
        )

        languagesFlow.value = listOf(randomLanguage(Locale.ENGLISH), randomLanguage(Locale.FRENCH))
        every { toolsRepository.getDownloadedToolsFlowByTypesAndLanguage(any(), Locale.ENGLISH) } returns toolsFlow

        presenter.test {
            assertNotNull(expectMostRecentItem().languages) {
                assertEquals(2, it.single { it.language.code == Locale.ENGLISH }.downloadedTools)
                assertEquals(0, it.single { it.language.code == Locale.FRENCH }.downloadedTools)
            }
        }
    }

    @Test
    fun `State - languages - totalTools`() = testScope.runTest {
        val toolsFlow = MutableStateFlow(
            listOf(
                randomTool("code1"),
                randomTool("code2")
            )
        )

        languagesFlow.value = listOf(randomLanguage(Locale.ENGLISH), randomLanguage(Locale.FRENCH))
        every { toolsRepository.getNormalToolsFlowByLanguage(Locale.ENGLISH) } returns toolsFlow

        presenter.test {
            assertNotNull(expectMostRecentItem().languages) {
                assertEquals(2, it.single { it.language.code == Locale.ENGLISH }.totalTools)
                assertEquals(0, it.single { it.language.code == Locale.FRENCH }.totalTools)
            }
        }
    }

    @Test
    fun `State - languages - float initially pinned languages to top`() = testScope.runTest {
        languagesFlow.value = listOf(
            randomLanguage(Locale.FRENCH, isForcedName = false, isAdded = false),
            randomLanguage(Locale.ENGLISH, isForcedName = false, isAdded = false),
            randomLanguage(Locale.GERMAN, isForcedName = false, isAdded = true)
        )

        presenter.test {
            assertEquals(
                listOf(Locale.GERMAN, Locale.ENGLISH, Locale.FRENCH),
                expectMostRecentItem().languages.map { it.language.code }
            )
        }
    }

    @Test
    fun `State - languages - filter by search query`() = testScope.runTest {
        presenter.test {
            val query = awaitItem().query
            languagesFlow.value = listOf(
                randomLanguage(Locale.ENGLISH, isForcedName = false),
                randomLanguage(Locale.FRENCH, isForcedName = false),
            )

            assertEquals(
                setOf(Locale.ENGLISH, Locale.FRENCH),
                expectMostRecentItem().languages.mapTo(mutableSetOf()) { it.language.code }
            )

            query.value = "eng"
            assertEquals(
                setOf(Locale.ENGLISH),
                expectMostRecentItem().languages.mapTo(mutableSetOf()) { it.language.code }
            )

            query.value = "fr"
            assertEquals(
                setOf(Locale.FRENCH),
                expectMostRecentItem().languages.mapTo(mutableSetOf()) { it.language.code }
            )
        }
    }
    // endregion State.languages

    @Test
    fun `Event - NavigateUp`() = testScope.runTest {
        presenter.test {
            awaitItem().eventSink(UiEvent.NavigateUp)

            navigator.awaitPop()
        }
    }
}
