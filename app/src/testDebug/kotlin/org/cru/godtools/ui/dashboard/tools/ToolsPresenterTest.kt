package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsPresenterTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)
    private val isFavoritesFeatureDiscovered = MutableStateFlow(true)
    private val metatoolsFlow = MutableStateFlow(emptyList<Tool>())
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val gospelLanguagesFlow = MutableStateFlow(emptyList<Language>())
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedLocale = MutableStateFlow<Locale?>(null)

    private val testScope = TestScope()

    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } returns flowOf(null)
        every { getLanguagesFlow() } returns languagesFlow
        every { getLanguagesFlowForToolCategory(Tool.CATEGORY_GOSPEL) } returns gospelLanguagesFlow

        excludeRecords { this@mockk.equals(any()) }
    }
    private val navigator = FakeNavigator(ToolsScreen)
    private val settings: Settings = mockk {
        every { appLanguage } returns this@ToolsPresenterTest.appLanguage.value
        every { appLanguageFlow } returns this@ToolsPresenterTest.appLanguage
        every { isFeatureDiscoveredFlow(Settings.FEATURE_TOOL_FAVORITE) } returns isFavoritesFeatureDiscovered
        every { getDashboardFilterCategoryFlow() } returns selectedCategory
        every { getDashboardFilterLocaleFlow() } returns selectedLocale
        coEvery { updateDashboardFilterCategory(any()) } answers { selectedCategory.value = firstArg() }
        coEvery { updateDashboardFilterLocale(any()) } answers { selectedLocale.value = firstArg() }
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns toolsFlow
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getMetaToolsFlow() } returns metatoolsFlow
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowForTools(any()) } returns flowOf(emptyList())
    }

    private val toolCardPresenter: ToolCardPresenter = mockk {
        everyComposable { present(tool = any(), secondLanguage = any(), eventSink = any()) }
            .answers { ToolCard.State(tool = firstArg()) }
    }

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
            translationsRepository = translationsRepository,
            ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
            navigator = navigator,
        )
    }

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    // region State.banner
    @Test
    fun `State - banner - none`() = testScope.runTest {
        presenter.test {
            isFavoritesFeatureDiscovered.value = true
            assertNull(expectMostRecentItem().banner)
        }
    }

    @Test
    fun `State - banner - favorites`() = testScope.runTest {
        presenter.test {
            isFavoritesFeatureDiscovered.value = false
            assertEquals(BannerType.TOOL_LIST_FAVORITES, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.spotlightTools
    @Test
    fun `Property spotlightTools`() = testScope.runTest {
        val normalTool = randomTool("normal", isHidden = false, isSpotlight = false)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.value = listOf(normalTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = testScope.runTest {
        val hiddenTool = randomTool("normal", isHidden = true, isSpotlight = true)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.value = listOf(hiddenTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = testScope.runTest {
        val tools = List(10) {
            randomTool("tool$it", Tool.Type.TRACT, defaultOrder = it, isHidden = false, isSpotlight = true)
        }

        presenter.test {
            toolsFlow.value = tools.shuffled()
            assertEquals(tools, expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }
    // endregion State.spotlightTools

    // region State.filters.categoryFilter.items
    @Test
    fun `State - filters - categoryFilter - items - no language`() = testScope.runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 0),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 1),
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_ARTICLES, 1)
                ),
                expectMostRecentItem().filters.categoryFilter.items
            )
        }
    }

    @Test
    fun `State - filters - categoryFilter - items - distinct categories`() = testScope.runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
        )

        presenter.test {
            assertEquals(
                listOf(FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 2)),
                expectMostRecentItem().filters.categoryFilter.items
            )
        }
    }

    @Test
    fun `State - filters - categoryFilter - items - ordered by tool default order`() = testScope.runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 1),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 0),
        )

        presenter.test {
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_ARTICLES, 1),
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 1)
                ),
                expectMostRecentItem().filters.categoryFilter.items
            )
        }
    }

    @Test
    fun `State - filters - categoryFilter - items - exclude non-default variants`() = testScope.runTest {
        val meta = randomTool("meta", defaultVariantCode = "tool")
        toolsFlow.value = listOf(
            randomTool("tool", category = Tool.CATEGORY_GOSPEL, metatoolCode = "meta", isHidden = false),
            randomTool("other", category = Tool.CATEGORY_ARTICLES, metatoolCode = "meta", isHidden = false),
        )

        presenter.test {
            metatoolsFlow.value = listOf(meta)
            assertEquals(
                listOf(FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 1)),
                expectMostRecentItem().filters.categoryFilter.items
            )
        }
    }

    @Test
    fun `State - filters - categoryFilter - items - exclude hidden tools`() = testScope.runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = true),
        )

        presenter.test {
            assertEquals(
                listOf(FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 1)),
                expectMostRecentItem().filters.categoryFilter.items
            )
        }
    }
    // endregion State.filters.categoryFilter.items

    // region State.filters.languageFilter.items
    @Test
    fun `State - filters - languageFilter - items - no category`() = testScope.runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            languagesFlow.value = languages
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0)
                ),
                expectMostRecentItem().filters.languageFilter.items
            )
        }

        verifyAll {
            languagesRepository.getLanguagesFlow()
        }
    }

    @Test
    fun `State - filters - languageFilter - items - for category`() = testScope.runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            awaitItem().filters.categoryFilter.eventSink(FilterMenu.Event.SelectItem(Tool.CATEGORY_GOSPEL))

            gospelLanguagesFlow.value = languages
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0)
                ),
                expectMostRecentItem().filters.languageFilter.items
            )
        }
    }

    @Test
    fun `State - filters - languageFilter - items - include tool count`() = testScope.runTest {
        val translationsFlow = MutableStateFlow(emptyList<Translation>())
        every { translationsRepository.getTranslationsFlowForTools(setOf("tool1", "tool2")) } returns translationsFlow

        presenter.test {
            toolsFlow.value = listOf(
                randomTool("tool1", metatoolCode = null, isHidden = false),
                randomTool("tool2", metatoolCode = null, isHidden = false),
            )
            translationsFlow.value = listOf(
                randomTranslation("tool1", Locale.ENGLISH),
                randomTranslation("tool1", Locale.FRENCH),
                randomTranslation("tool2", Locale.ENGLISH, version = 1),
                randomTranslation("tool2", Locale.ENGLISH, version = 2),
            )
            languagesFlow.value = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 2),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 1)
                ),
                expectMostRecentItem().filters.languageFilter.items
            )
        }
    }

    @Test
    fun `State - filters - languageFilter - items - filtered by query`() = testScope.runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))
        languagesFlow.value = languages

        presenter.test {
            expectMostRecentItem().filters.languageFilter.let {
                it.menuExpanded.value = true
                assertEquals(
                    listOf(
                        FilterMenu.UiState.Item(null, 0),
                        FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                        FilterMenu.UiState.Item(Language(Locale.FRENCH), 0),
                    ),
                    it.items
                )
                it.query.value = "french"
            }

            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0)
                ),
                expectMostRecentItem().filters.languageFilter.items
            )
        }

        verifyAll {
            languagesRepository.getLanguagesFlow()
        }
    }
    // endregion State.filters.languageFilter.items

    // region State.filters.languageFilter.selectedItem
    @Test
    fun `State - filters - languageFilter - selectedItem - no language selected`() = testScope.runTest {
        presenter.test {
            assertNull(expectMostRecentItem().filters.languageFilter.selectedItem)
        }

        verify(inverse = true) { languagesRepository.findLanguageFlow(any()) }
    }

    @Test
    fun `State - filters - languageFilter - selectedItem - language not found`() = testScope.runTest {
        presenter.test {
            awaitItem().filters.languageFilter.eventSink(FilterMenu.Event.SelectItem(Language(Locale.ENGLISH)))

            assertNull(expectMostRecentItem().filters.languageFilter.selectedItem)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }

    @Test
    fun `State - filters - languageFilter - selectedItem - language selected`() = testScope.runTest {
        val language = Language(Locale.ENGLISH)
        every { languagesRepository.findLanguageFlow(Locale.ENGLISH) } returns flowOf(language)

        presenter.test {
            awaitItem().filters.languageFilter.eventSink(FilterMenu.Event.SelectItem(language))

            assertEquals(language, expectMostRecentItem().filters.languageFilter.selectedItem)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }
    // endregion State.filters.languageFilter.selectedItem

    // region State.filters.languageFilter.menuExpanded
    @Test
    fun `State - filters - languageFilter - menuExpanded - resets query when set to false`() = testScope.runTest {
        presenter.test {
            val state = expectMostRecentItem().filters.languageFilter

            state.menuExpanded.value = true
            state.query.value = "test"
            assertEquals("test", state.query.value)

            state.menuExpanded.value = false
            assertEquals("", state.query.value)

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion FiltersEvent.ToggleLanguagesMenu

    // region State.tools
    @Test
    fun `State - tools - return only default variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META, defaultVariantCode = "variant2")
        val variant1 = Tool("variant1", metatoolCode = "meta")
        val variant2 = Tool("variant2", metatoolCode = "meta")

        presenter.test {
            assertEquals(emptyList(), awaitItem().tools)

            metatoolsFlow.value = listOf(meta)
            toolsFlow.value = listOf(variant1, variant2)
            assertEquals(listOf(variant2), expectMostRecentItem().tools)
        }
    }

    @Test
    fun `State - tools - Don't return hidden tools`() = testScope.runTest {
        val hidden = randomTool("hidden", isHidden = true, metatoolCode = null)
        val visible = randomTool("visible", isHidden = false, metatoolCode = null)

        presenter.test {
            assertEquals(emptyList(), awaitItem().tools)

            toolsFlow.value = listOf(hidden, visible)
            assertEquals(listOf(visible), expectMostRecentItem().tools)
        }
    }
    // endregion State.tools
}
