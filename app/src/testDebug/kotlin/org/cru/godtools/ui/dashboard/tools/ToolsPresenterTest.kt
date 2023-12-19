package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.ui.banner.BannerType
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsPresenterTest {
    private val isFavoritesFeatureDiscovered = MutableStateFlow(true)

    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } returns flowOf(null)
    }
    private val navigator = FakeNavigator()
    private val settings: Settings = mockk {
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) } returns isFavoritesFeatureDiscovered
    }

    private val presenter = ToolsPresenter(
        settings = settings,
        languagesRepository = languagesRepository,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    // region State.banner
    @Test
    fun `State - banner - none`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(banner = presenter.rememberBanner(), eventSink = {})
            }
        ) {
            isFavoritesFeatureDiscovered.value = true
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(banner = presenter.rememberBanner(), eventSink = {})
            }
        ) {
            isFavoritesFeatureDiscovered.value = false
            assertEquals(BannerType.TOOL_LIST_FAVORITES, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.filters.selectedLanguage
    @Test
    fun `State - filters - selectedLanguage - no language selected`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(
                    filters = ToolsScreen.State.Filters(
                        selectedLanguage = presenter.rememberLanguage(null)
                    ),
                    eventSink = {}
                )
            }
        ) {
            assertNull(expectMostRecentItem().filters.selectedLanguage)
        }

        verifyAll { languagesRepository wasNot Called }
    }

    @Test
    fun `State - filters - selectedLanguage - language not found`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(
                    filters = ToolsScreen.State.Filters(
                        selectedLanguage = presenter.rememberLanguage(Locale.ENGLISH)
                    ),
                    eventSink = {}
                )
            }
        ) {
            assertNull(expectMostRecentItem().filters.selectedLanguage)
        }

        verifyAll { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }

    @Test
    fun `State - filters - selectedLanguage - language selected`() = runTest {
        val language = Language(Locale.ENGLISH)
        every { languagesRepository.findLanguageFlow(Locale.ENGLISH) } returns flowOf(language)

        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(
                    filters = ToolsScreen.State.Filters(
                        selectedLanguage = presenter.rememberLanguage(Locale.ENGLISH)
                    ),
                    eventSink = {}
                )
            }
        ) {
            assertEquals(language, expectMostRecentItem().filters.selectedLanguage)
        }

        verifyAll { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }
    // endregion State.filters.selectedLanguage
}
