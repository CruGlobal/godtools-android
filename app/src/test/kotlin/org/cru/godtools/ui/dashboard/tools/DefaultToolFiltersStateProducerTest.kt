package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.presenterTestOf
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
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@Suppress("UnusedFlow")
@OptIn(ExperimentalCoroutinesApi::class)
class DefaultToolFiltersStateProducerTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)
    private val filteredToolsFlow = MutableStateFlow(emptyList<Tool>())
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val gospelLanguagesFlow = MutableStateFlow(emptyList<Language>())
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedLocale = MutableStateFlow<Locale?>(null)

    private val testScope = TestScope()

    private val filteredToolsFlowProducer: FilteredToolsFlowProducer = mockk {
        every { getFlow(any(), any(), any()) } returns filteredToolsFlow
    }
    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } returns flowOf(null)
        every { getLanguagesFlow() } returns languagesFlow
        every { getLanguagesFlowForToolCategory(Tool.CATEGORY_GOSPEL) } returns gospelLanguagesFlow

        excludeRecords { this@mockk.equals(any()) }
    }
    private val settings: Settings = mockk {
        every { appLanguage } returns this@DefaultToolFiltersStateProducerTest.appLanguage.value
        every { appLanguageFlow } returns this@DefaultToolFiltersStateProducerTest.appLanguage
        every { getDashboardFilterCategoryFlow() } returns selectedCategory
        every { getDashboardFilterLocaleFlow() } returns selectedLocale
        coEvery { updateDashboardFilterCategory(any()) } answers { selectedCategory.value = firstArg() }
        coEvery { updateDashboardFilterLocale(any()) } answers { selectedLocale.value = firstArg() }
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowForTools(any()) } returns flowOf(emptyList())
    }

    private lateinit var producer: DefaultToolFiltersStateProducer

    @BeforeTest
    fun setup() {
        producer = DefaultToolFiltersStateProducer(
            context = ApplicationProvider.getApplicationContext(),
            filteredToolsFlowProducer = filteredToolsFlowProducer,
            languagesRepository = languagesRepository,
            settings = settings,
            translationsRepository = translationsRepository,
            ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
        )
    }

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    // region Filters.categoryFilter.items
    @Test
    fun `Filters - categoryFilter - items - distinct & ordered by tool order`() = testScope.runTest {
        filteredToolsFlow.value = listOf(
            randomTool(category = Tool.CATEGORY_ARTICLES),
            randomTool(category = Tool.CATEGORY_GOSPEL),
            randomTool(category = Tool.CATEGORY_ARTICLES),
        )

        presenterTestOf(presentFunction = { producer.produce() }) {
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_ARTICLES, 2),
                    FilterMenu.UiState.Item<String?>(Tool.CATEGORY_GOSPEL, 1)
                ),
                expectMostRecentItem().categoryFilter.items
            )
        }
    }

    @Test
    fun `Filters - categoryFilter - items - uses selected language`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            awaitItem().languageFilter.eventSink(FilterMenu.Event.SelectItem(Language(Locale.FRENCH)))
            expectMostRecentItem()

            verify { filteredToolsFlowProducer.getFlow(mode = Mode.ALL_TOOLS, language = Locale.FRENCH) }
        }
    }
    // endregion Filters.categoryFilter.items

    // region Filters.languageFilter.items
    @Test
    fun `Filters - languageFilter - items - no category`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            languagesFlow.value = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0)
                ),
                expectMostRecentItem().languageFilter.items
            )
        }

        verifyAll { languagesRepository.getLanguagesFlow() }
    }

    @Test
    fun `Filters - languageFilter - items - for category`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            awaitItem().categoryFilter.eventSink(FilterMenu.Event.SelectItem(Tool.CATEGORY_GOSPEL))

            gospelLanguagesFlow.value = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(null, 0),
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 0),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 0)
                ),
                expectMostRecentItem().languageFilter.items
            )
        }
    }

    @Test
    fun `Filters - languageFilter - items - include tool count`() = testScope.runTest {
        val translationsFlow = MutableStateFlow(emptyList<Translation>())
        every { translationsRepository.getTranslationsFlowForTools(setOf("tool1", "tool2")) } returns translationsFlow

        presenterTestOf(presentFunction = { producer.produce() }) {
            filteredToolsFlow.value = listOf(
                randomTool("tool1", isHidden = false),
                randomTool("tool2", isHidden = false),
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
                expectMostRecentItem().languageFilter.items
            )
        }
    }

    @Test
    fun `Filters - languageFilter - items - filtered by query`() = testScope.runTest {
        languagesFlow.value = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))

        presenterTestOf(presentFunction = { producer.produce() }) {
            expectMostRecentItem().languageFilter.let {
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
                expectMostRecentItem().languageFilter.items
            )
        }

        verifyAll { languagesRepository.getLanguagesFlow() }
    }
    // endregion Filters.languageFilter.items

    // region Filters.languageFilter.selectedItem
    @Test
    fun `Filters - languageFilter - selectedItem - no language selected`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            assertNull(expectMostRecentItem().languageFilter.selectedItem)
        }

        verify(inverse = true) { languagesRepository.findLanguageFlow(any()) }
    }

    @Test
    fun `Filters - languageFilter - selectedItem - language not found`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            awaitItem().languageFilter.eventSink(FilterMenu.Event.SelectItem(Language(Locale.ENGLISH)))

            assertNull(expectMostRecentItem().languageFilter.selectedItem)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }

    @Test
    fun `Filters - languageFilter - selectedItem - language selected`() = testScope.runTest {
        val language = Language(Locale.ENGLISH)
        every { languagesRepository.findLanguageFlow(Locale.ENGLISH) } returns flowOf(language)

        presenterTestOf(presentFunction = { producer.produce() }) {
            awaitItem().languageFilter.eventSink(FilterMenu.Event.SelectItem(language))

            assertEquals(language, expectMostRecentItem().languageFilter.selectedItem)
        }

        verify { languagesRepository.findLanguageFlow(Locale.ENGLISH) }
    }
    // endregion Filters.languageFilter.selectedItem

    // region Filters.languageFilter.menuExpanded
    @Test
    fun `Filters - languageFilter - menuExpanded - resets query when set to false`() = testScope.runTest {
        presenterTestOf(presentFunction = { producer.produce() }) {
            val state = expectMostRecentItem().languageFilter

            state.menuExpanded.value = true
            state.query.value = "test"
            assertEquals("test", state.query.value)

            state.menuExpanded.value = false
            assertEquals("", state.query.value)

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion Filters.languageFilter.menuExpanded
}
