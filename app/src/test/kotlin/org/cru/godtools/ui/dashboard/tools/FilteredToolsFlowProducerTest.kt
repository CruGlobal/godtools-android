package org.cru.godtools.ui.dashboard.tools

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState.Mode

@Suppress("UnusedFlow")
class FilteredToolsFlowProducerTest {
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val countryFlow = MutableStateFlow<String?>(null)
    private val normalToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@FilteredToolsFlowProducerTest.appLanguageFlow
        every { getPersonalizationCountryFlow() } returns countryFlow
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns normalToolsFlow
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getPersonalizedToolsFlow(any(), any()) } returns flowOf(emptyList())
    }

    private val producer = FilteredToolsFlowProducer(
        settings = settings,
        toolsRepository = toolsRepository
    )

    // region language selection
    @Test
    fun `getFlow - All Tools - no language - uses getNormalToolsFlow`() = runTest {
        producer.getFlow(mode = Mode.ALL_TOOLS, language = null).first()
        verify { toolsRepository.getNormalToolsFlow() }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlowByLanguage(any()) }
    }

    @Test
    fun `getFlow - All Tools - with language - uses getNormalToolsFlowByLanguage`() = runTest {
        producer.getFlow(mode = Mode.ALL_TOOLS, language = Locale.FRENCH).first()
        verify { toolsRepository.getNormalToolsFlowByLanguage(Locale.FRENCH) }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlow() }
    }
    // endregion language selection

    // region sort order
    @Test
    fun `getFlow - All Tools - no language - tools sorted by defaultOrder`() = runTest {
        val tools = List(5) { createTool(defaultOrder = it) }

        normalToolsFlow.value = tools.shuffled()
        assertEquals(tools, producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }

    @Test
    fun `getFlow - All Tools - with language - tools sorted by defaultOrder`() = runTest {
        val tools = List(5) { createTool(defaultOrder = it) }
        every { toolsRepository.getNormalToolsFlowByLanguage(Locale.FRENCH) } returns flowOf(tools.shuffled())

        assertEquals(tools, producer.getFlow(mode = Mode.ALL_TOOLS, language = Locale.FRENCH).first())
    }
    // endregion sort order

    // region hidden filter
    @Test
    fun `getFlow - hidden tools are excluded`() = runTest {
        val hidden = createTool(isHidden = true)
        val visible = createTool(isHidden = false)

        normalToolsFlow.value = listOf(hidden, visible)
        assertEquals(listOf(visible), producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }

    @Test
    fun `getFlow - visible tools are included`() = runTest {
        val tool = createTool(isHidden = false)

        normalToolsFlow.value = listOf(tool)
        assertEquals(listOf(tool), producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }
    // endregion hidden filter

    // region category filter
    @Test
    fun `getFlow - null category includes all tools`() = runTest {
        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(gospel, articles)
        assertEquals(setOf(gospel, articles), producer.getFlow(mode = Mode.ALL_TOOLS, category = null).first().toSet())
    }

    @Test
    fun `getFlow - category filter includes only matching tools`() = runTest {
        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(gospel, articles)
        assertEquals(listOf(gospel), producer.getFlow(mode = Mode.ALL_TOOLS, category = Tool.CATEGORY_GOSPEL).first())
    }

    @Test
    fun `getFlow - category filter excludes tools with different category`() = runTest {
        val tool = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(tool)
        assertEquals(emptyList(), producer.getFlow(mode = Mode.ALL_TOOLS, category = Tool.CATEGORY_GOSPEL).first())
    }
    // endregion category filter

    // region combined filters
    @Test
    fun `getFlow - All Tools - category and language filters applied together`() = runTest {
        val languageToolsFlow = MutableStateFlow(emptyList<Tool>())
        every { toolsRepository.getNormalToolsFlowByLanguage(Locale.FRENCH) } returns languageToolsFlow

        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        languageToolsFlow.value = listOf(gospel, articles)
        assertEquals(
            listOf(gospel),
            producer.getFlow(mode = Mode.ALL_TOOLS, category = Tool.CATEGORY_GOSPEL, language = Locale.FRENCH).first()
        )
    }
    // endregion combined filters

    // region Personalization mode
    @Test
    fun `getFlow - Personalization mode - uses getPersonalizedToolsFlow`() = runTest {
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getPersonalizedToolsFlow(any(), any()) }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlow() }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlowByLanguage(any()) }
    }

    @Test
    fun `getFlow - Personalization mode - uses appLanguageFlow when no language provided`() = runTest {
        appLanguageFlow.value = Locale.FRENCH
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getPersonalizedToolsFlow(Locale.FRENCH, any()) }
    }

    @Test
    fun `getFlow - Personalization mode - uses provided language when specified`() = runTest {
        producer.getFlow(mode = Mode.PERSONALIZATION, language = Locale.GERMAN).first()
        verify { toolsRepository.getPersonalizedToolsFlow(Locale.GERMAN, any()) }
        verify(exactly = 0) { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, any()) }
    }

    @Test
    fun `getFlow - Personalization mode - uses Settings getPersonalizationCountryFlow for country`() = runTest {
        countryFlow.value = "US"
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getPersonalizedToolsFlow(any(), "US") }
    }

    @Test
    fun `getFlow - Personalization mode - returns personalized tools when non-empty`() = runTest {
        val tool = createTool()
        val fallbackTool = createTool()
        countryFlow.value = "US"
        every { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(tool))
        every { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallbackTool))

        assertEquals(listOf(tool), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization mode - falls back to language only when no country-specific tools`() = runTest {
        val fallbackTool = createTool()
        countryFlow.value = "US"
        every { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(emptyList())
        every { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallbackTool))

        assertEquals(listOf(fallbackTool), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization mode - hidden tools are excluded`() = runTest {
        val hidden = createTool(isHidden = true)
        val visible = createTool(isHidden = false)
        every { toolsRepository.getPersonalizedToolsFlow(any(), any()) } returns flowOf(listOf(hidden, visible))

        assertEquals(listOf(visible), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization mode - category filter applies`() = runTest {
        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)
        every { toolsRepository.getPersonalizedToolsFlow(any(), any()) } returns flowOf(listOf(gospel, articles))

        assertEquals(
            listOf(gospel),
            producer.getFlow(mode = Mode.PERSONALIZATION, category = Tool.CATEGORY_GOSPEL).first()
        )
    }

    @Test
    fun `getFlow - Personalization mode - updates when appLanguage changes`() = runTest {
        val frenchTool = createTool()
        every { toolsRepository.getPersonalizedToolsFlow(Locale.FRENCH, null) } returns flowOf(listOf(frenchTool))

        producer.getFlow(mode = Mode.PERSONALIZATION).test {
            assertEquals(emptyList(), awaitItem())

            appLanguageFlow.value = Locale.FRENCH
            assertEquals(listOf(frenchTool), awaitItem())
        }
    }

    @Test
    fun `getFlow - Personalization mode - updates when country changes`() = runTest {
        val usTool = createTool()
        every { toolsRepository.getPersonalizedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(usTool))

        producer.getFlow(mode = Mode.PERSONALIZATION).test {
            assertEquals(emptyList(), awaitItem())

            countryFlow.value = "US"
            assertEquals(listOf(usTool), awaitItem())
        }
    }
    // endregion Personalization mode

    private fun createTool(category: String? = null, defaultOrder: Int = 0, isHidden: Boolean = false) = randomTool(
        category = category,
        defaultOrder = defaultOrder,
        isHidden = isHidden,
    )
}
