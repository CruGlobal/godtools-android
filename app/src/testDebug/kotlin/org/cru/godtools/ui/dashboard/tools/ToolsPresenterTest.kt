package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
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
import org.cru.godtools.ui.dashboard.tools.ToolsScreen.Filters.Filter
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsPresenterTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)
    private val isFavoritesFeatureDiscovered = MutableStateFlow(true)
    private val metatoolsFlow = MutableStateFlow(emptyList<Tool>())
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val gospelLanguagesFlow = MutableStateFlow(emptyList<Language>())

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
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getMetaToolsFlow() } returns metatoolsFlow
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowForTools(any()) } returns flowOf(emptyList())
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
            translationsRepository = translationsRepository,
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
            toolsFlow.value = listOf(normalTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Don't show hidden tools`() = runTest {
        val hiddenTool = randomTool("normal", isHidden = true, isSpotlight = true)
        val spotlightTool = randomTool("spotlight", isHidden = false, isSpotlight = true)

        presenter.test {
            toolsFlow.value = listOf(hiddenTool, spotlightTool)
            assertEquals(listOf(spotlightTool), expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }

    @Test
    fun `Property spotlightTools - Sorted by default order`() = runTest {
        val tools = List(10) {
            randomTool("tool$it", Tool.Type.TRACT, defaultOrder = it, isHidden = false, isSpotlight = true)
        }

        presenter.test {
            toolsFlow.value = tools.shuffled()
            assertEquals(tools, expectMostRecentItem().spotlightTools.map { it.tool })
        }
    }
    // endregion State.spotlightTools

    // region State.filters.categories
    @Test
    fun `State - filters - categories - no language`() = runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 0),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            assertEquals(
                listOf(Filter(Tool.CATEGORY_GOSPEL, 1), Filter(Tool.CATEGORY_ARTICLES, 1)),
                expectMostRecentItem().filters.categories
            )
        }
    }

    @Test
    fun `State - filters - categories - distinct categories`() = runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
        )

        presenter.test {
            assertEquals(listOf(Filter(Tool.CATEGORY_GOSPEL, 2)), expectMostRecentItem().filters.categories)
        }
    }

    @Test
    fun `State - filters - categories - ordered by tool default order`() = runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false, defaultOrder = 1),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = false, defaultOrder = 0),
        )

        presenter.test {
            assertEquals(
                listOf(Filter(Tool.CATEGORY_ARTICLES, 1), Filter(Tool.CATEGORY_GOSPEL, 1)),
                expectMostRecentItem().filters.categories
            )
        }
    }

    @Test
    fun `State - filters - categories - exclude non-default variants`() = runTest {
        val meta = randomTool("meta", defaultVariantCode = "tool")
        toolsFlow.value = listOf(
            randomTool("tool", category = Tool.CATEGORY_GOSPEL, metatoolCode = "meta", isHidden = false),
            randomTool("other", category = Tool.CATEGORY_ARTICLES, metatoolCode = "meta", isHidden = false),
        )

        presenter.test {
            metatoolsFlow.value = listOf(meta)
            assertEquals(listOf(Filter(Tool.CATEGORY_GOSPEL, 1)), expectMostRecentItem().filters.categories)
        }
    }

    @Test
    fun `State - filters - categories - exclude hidden tools`() = runTest {
        toolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_GOSPEL, metatoolCode = null, isHidden = false),
            randomTool(category = Tool.CATEGORY_ARTICLES, metatoolCode = null, isHidden = true),
        )

        presenter.test {
            assertEquals(listOf(Filter(Tool.CATEGORY_GOSPEL, 1)), expectMostRecentItem().filters.categories)
        }
    }
    // endregion State.filters.categories

    // region State.filters.languages
    @Test
    fun `State - filters - languages - no category`() = runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            languagesFlow.value = languages
            assertEquals(languages.map { Filter(it, 0) }, expectMostRecentItem().filters.languages)
        }

        verifyAll {
            languagesRepository.getLanguagesFlow()
        }
    }

    @Test
    fun `State - filters - languages - for category`() = runTest {
        val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenter.test {
            awaitItem().filters.eventSink(ToolsScreen.FiltersEvent.SelectCategory(Tool.CATEGORY_GOSPEL))

            gospelLanguagesFlow.value = languages
            assertEquals(languages.map { Filter(it, 0) }, expectMostRecentItem().filters.languages)
        }
    }

    @Test
    fun `State - filters - languages - include tool count`() = runTest {
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
                listOf(Filter(Language(Locale.ENGLISH), 2), Filter(Language(Locale.FRENCH), 1)),
                expectMostRecentItem().filters.languages
            )
        }
    }
    // endregion State.filters.languages

    // region State.filters.selectedLanguage
    @Test
    fun `State - filters - selectedLanguage - no language selected`() = runTest {
        presenter.test {
            assertNull(expectMostRecentItem().filters.selectedLanguage)
        }

        verify(inverse = true) { languagesRepository.findLanguageFlow(any()) }
    }

    @Test
    fun `State - filters - selectedLanguage - language not found`() = runTest {
        presenter.test {
            awaitItem().filters.eventSink(ToolsScreen.FiltersEvent.SelectLanguage(Locale.ENGLISH))

            assertNull(expectMostRecentItem().filters.selectedLanguage)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }

    @Test
    fun `State - filters - selectedLanguage - language selected`() = runTest {
        val language = Language(Locale.ENGLISH)
        every { languagesRepository.findLanguageFlow(Locale.ENGLISH) } returns flowOf(language)

        presenter.test {
            awaitItem().filters.eventSink(ToolsScreen.FiltersEvent.SelectLanguage(Locale.ENGLISH))

            assertEquals(language, expectMostRecentItem().filters.selectedLanguage)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
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

            metatoolsFlow.value = listOf(meta)
            toolsFlow.value = listOf(variant1, variant2)
            assertEquals(listOf(variant2), expectMostRecentItem().tools)
        }
    }

    @Test
    fun `State - tools - Don't return hidden tools`() = runTest {
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
