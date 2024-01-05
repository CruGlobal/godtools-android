package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.presenterTestOf
import com.slack.circuit.test.test
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsPresenterTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)
    private val isFavoritesFeatureDiscovered = MutableStateFlow(true)
    private val metatoolsFlow = MutableSharedFlow<List<Tool>>(extraBufferCapacity = 1)
    private val toolsFlow = MutableSharedFlow<List<Tool>>(extraBufferCapacity = 1)
    private val languagesFlow = MutableSharedFlow<List<Language>>(extraBufferCapacity = 1)
    private val gospelLanguagesFlow = MutableSharedFlow<List<Language>>(extraBufferCapacity = 1)

    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } returns flowOf(null)
        every { getLanguagesFlow() } returns languagesFlow
        every { getLanguagesFlowForToolCategory(Tool.CATEGORY_GOSPEL) } returns gospelLanguagesFlow
    }
    private val navigator = FakeNavigator()
    private val settings: Settings = mockk {
        every { appLanguage } returns this@ToolsPresenterTest.appLanguage.value
        every { appLanguageFlow } returns this@ToolsPresenterTest.appLanguage
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) } returns isFavoritesFeatureDiscovered
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns toolsFlow
        every { getMetaToolsFlow() } returns metatoolsFlow
    }

    // TODO: figure out how to mock ToolCardPresenter
    private val toolCardPresenter = ToolCardPresenter(
        fileSystem = mockk(),
        settings = mockk(relaxed = true),
        attachmentsRepository = mockk(relaxed = true),
        toolsRepository = mockk(),
        translationsRepository = mockk(relaxed = true),
    )

    private lateinit var presenter: ToolsPresenter

    @BeforeTest
    fun setup() {
        presenter = ToolsPresenter(
            context = ApplicationProvider.getApplicationContext(),
            eventBus = mockk(),
            settings = settings,
            toolCardPresenter = toolCardPresenter,
            languagesRepository = languagesRepository,
            toolsRepository = toolsRepository,
            navigator = navigator,
        )
    }

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    // region State.banner
    @Test
    fun `State - banner - none`() = runTest {
        presenter.test {
            isFavoritesFeatureDiscovered.value = true
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = runTest {
        presenter.test {
            isFavoritesFeatureDiscovered.value = false
            assertEquals(BannerType.TOOL_LIST_FAVORITES, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.spotlightTools
    @Test
    fun `Property spotlightTools`() = runTest {
        val normalTool = randomTool("normal", isHidden = false, isSpotlight = false)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.emit(listOf(normalTool, spotlightTool))
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = runTest {
        val hiddenTool = randomTool("normal", isHidden = true, isSpotlight = true)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.emit(listOf(hiddenTool, spotlightTool))
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = runTest {
        val tools = List(10) {
            randomTool("tool$it", Tool.Type.TRACT, defaultOrder = it, isHidden = false, isSpotlight = true)
        }

        presenter.test {
            toolsFlow.emit(tools.shuffled())
            assertEquals(tools, expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }
    // endregion State.spotlightTools

    // region State.filters.categories
    @Test
    fun `State - filters - categories - no language`() = runTest {
        val tools = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 0),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            metatoolsFlow.emit(emptyList())
            toolsFlow.emit(tools)
            assertEquals(
                listOf(Tool.CATEGORY_GOSPEL, Tool.CATEGORY_ARTICLES),
                expectMostRecentItem().filters.categories
            )
        }
    }

    @Test
    fun `State - filters - categories - distinct categories`() = runTest {
        val tools = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
        )

        presenter.test {
            metatoolsFlow.emit(emptyList())
            toolsFlow.emit(tools)
            assertEquals(listOf(Tool.CATEGORY_GOSPEL), expectMostRecentItem().filters.categories)
        }
    }

    @Test
    fun `State - filters - categories - ordered by tool default order`() = runTest {
        val tools = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 1),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 0),
        )

        presenter.test {
            metatoolsFlow.emit(emptyList())
            toolsFlow.emit(tools)
            assertEquals(
                listOf(Tool.CATEGORY_ARTICLES, Tool.CATEGORY_GOSPEL),
                expectMostRecentItem().filters.categories
            )
        }
    }

    @Test
    fun `State - filters - categories - exclude non-default variants`() = runTest {
        val meta = randomTool("meta", defaultVariantCode = "tool")
        val tools = listOf(
            randomTool("tool", category = Tool.CATEGORY_GOSPEL, metatoolCode = "meta", isHidden = false),
            randomTool("other", category = Tool.CATEGORY_ARTICLES, metatoolCode = "meta", isHidden = false),
        )

        presenter.test {
            metatoolsFlow.emit(listOf(meta))
            toolsFlow.emit(tools)
            assertEquals(listOf(Tool.CATEGORY_GOSPEL), expectMostRecentItem().filters.categories)
        }
    }

    @Test
    fun `State - filters - categories - exclude hidden tools`() = runTest {
        val tools = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = true),
        )

        presenter.test {
            metatoolsFlow.emit(emptyList())
            toolsFlow.emit(tools)
            assertEquals(listOf(Tool.CATEGORY_GOSPEL), expectMostRecentItem().filters.categories)
        }
    }
    // endregion State.filters.categories

    // region State.filters.languages
    @Test
    fun `State - filters - languages - no category`() = runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            languagesFlow.emit(languages)
            assertEquals(languages, expectMostRecentItem().filters.languages)
        }

        verifyAll {
            languagesRepository.getLanguagesFlow()
        }
    }

    @Test
    fun `State - filters - languages - for category`() = runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            awaitItem().eventSink(ToolsScreen.Event.UpdateSelectedCategory(Tool.CATEGORY_GOSPEL))

            gospelLanguagesFlow.emit(languages)
            assertEquals(languages, expectMostRecentItem().filters.languages)
        }
    }
    // endregion State.filters.languages

    // region State.filters.selectedLanguage
    @Test
    fun `State - filters - selectedLanguage - no language selected`() = runTest {
        presenterTestOf(
            presentFunction = {
                ToolsScreen.State(
                    filters = ToolsScreen.Filters(
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
                    filters = ToolsScreen.Filters(
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
                    filters = ToolsScreen.Filters(
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

    // region State.tools
    @Test
    fun `State - tools - return only default variants`() = runTest {
        val meta = Tool("meta", Tool.Type.META, defaultVariantCode = "variant2")
        val variant1 = Tool("variant1", metatoolCode = "meta")
        val variant2 = Tool("variant2", metatoolCode = "meta")

        presenter.test {
            assertEquals(emptyList(), awaitItem().tools)

            metatoolsFlow.emit(listOf(meta))
            toolsFlow.emit(listOf(variant1, variant2))
            assertEquals(listOf(variant2), expectMostRecentItem().tools)
        }
    }

    @Test
    fun `State - tools - Don't return hidden tools`() = runTest {
        val hidden = randomTool("hidden", isHidden = true, metatoolCode = null)
        val visible = randomTool("visible", isHidden = false, metatoolCode = null)

        presenter.test {
            assertEquals(emptyList(), awaitItem().tools)

            metatoolsFlow.emit(emptyList())
            toolsFlow.emit(listOf(hidden, visible))
            assertEquals(listOf(visible), expectMostRecentItem().tools)
        }
    }
    // endregion State.tools
}
