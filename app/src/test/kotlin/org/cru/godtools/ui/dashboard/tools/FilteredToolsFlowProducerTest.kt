package org.cru.godtools.ui.dashboard.tools

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
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool

@Suppress("UnusedFlow")
class FilteredToolsFlowProducerTest {
    private val normalToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns normalToolsFlow
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
    }

    private val producer = FilteredToolsFlowProducer(toolsRepository)

    // region language selection
    @Test
    fun `getFlow - no language - uses getNormalToolsFlow`() = runTest {
        producer.getFlow(language = null).first()
        verify { toolsRepository.getNormalToolsFlow() }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlowByLanguage(any()) }
    }

    @Test
    fun `getFlow - with language - uses getNormalToolsFlowByLanguage`() = runTest {
        producer.getFlow(language = Locale.FRENCH).first()
        verify { toolsRepository.getNormalToolsFlowByLanguage(Locale.FRENCH) }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlow() }
    }
    // endregion language selection

    // region hidden filter
    @Test
    fun `getFlow - hidden tools are excluded`() = runTest {
        val hidden = createTool(isHidden = true)
        val visible = createTool(isHidden = false)

        normalToolsFlow.value = listOf(hidden, visible)
        assertEquals(listOf(visible), producer.getFlow().first())
    }

    @Test
    fun `getFlow - visible tools are included`() = runTest {
        val tool = createTool(isHidden = false)

        normalToolsFlow.value = listOf(tool)
        assertEquals(listOf(tool), producer.getFlow().first())
    }
    // endregion hidden filter

    // region sort order
    @Test
    fun `getFlow - tools sorted by defaultOrder`() = runTest {
        val tools = List(5) { createTool(defaultOrder = it) }

        normalToolsFlow.value = tools.shuffled()
        assertEquals(tools, producer.getFlow().first())
    }
    // endregion sort order

    // region category filter
    @Test
    fun `getFlow - null category includes all tools`() = runTest {
        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(gospel, articles)
        assertEquals(setOf(gospel, articles), producer.getFlow(category = null).first().toSet())
    }

    @Test
    fun `getFlow - category filter includes only matching tools`() = runTest {
        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(gospel, articles)
        assertEquals(listOf(gospel), producer.getFlow(category = Tool.CATEGORY_GOSPEL).first())
    }

    @Test
    fun `getFlow - category filter excludes tools with different category`() = runTest {
        val tool = createTool(category = Tool.CATEGORY_ARTICLES)

        normalToolsFlow.value = listOf(tool)
        assertEquals(emptyList(), producer.getFlow(category = Tool.CATEGORY_GOSPEL).first())
    }
    // endregion category filter

    // region combined filters
    @Test
    fun `getFlow - category and language filters applied together`() = runTest {
        val languageToolsFlow = MutableStateFlow(emptyList<Tool>())
        every { toolsRepository.getNormalToolsFlowByLanguage(Locale.FRENCH) } returns languageToolsFlow

        val gospel = createTool(category = Tool.CATEGORY_GOSPEL)
        val articles = createTool(category = Tool.CATEGORY_ARTICLES)

        languageToolsFlow.value = listOf(gospel, articles)
        assertEquals(
            listOf(gospel),
            producer.getFlow(category = Tool.CATEGORY_GOSPEL, language = Locale.FRENCH).first()
        )
    }
    // endregion combined filters

    private fun createTool(category: String? = null, defaultOrder: Int = 0, isHidden: Boolean = false) = randomTool(
        category = category,
        defaultOrder = defaultOrder,
        isHidden = isHidden,
    )
}
