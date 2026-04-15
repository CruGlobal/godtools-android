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
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool

@Suppress("UnusedFlow")
class FilteredToolsFlowProducerTest {
    private val metatoolsFlow = MutableStateFlow(emptyList<Tool>())
    private val normalToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns normalToolsFlow
        every { getNormalToolsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getMetaToolsFlow() } returns metatoolsFlow
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

    // region default variant filtering
    @Test
    fun `getFlow - non-variant tools always included`() = runTest {
        val tool = createTool(metatoolCode = null)

        normalToolsFlow.value = listOf(tool)
        assertEquals(listOf(tool), producer.getFlow().first())
    }

    @Test
    fun `getFlow - default variant is included`() = runTest {
        val meta = randomTool("meta", type = Tool.Type.META, defaultVariantCode = "default")
        val defaultVariant = randomTool("default", metatoolCode = "meta", isHidden = false)

        metatoolsFlow.value = listOf(meta)
        normalToolsFlow.value = listOf(defaultVariant)
        assertEquals(listOf(defaultVariant), producer.getFlow().first())
    }

    @Test
    fun `getFlow - non-default variant is excluded`() = runTest {
        val meta = randomTool("meta", type = Tool.Type.META, defaultVariantCode = "default")
        val nonDefault = randomTool("other", metatoolCode = "meta", isHidden = false)

        metatoolsFlow.value = listOf(meta)
        normalToolsFlow.value = listOf(nonDefault)
        assertEquals(emptyList(), producer.getFlow().first())
    }

    @Test
    fun `getFlow - variant with no matching metatool is excluded`() = runTest {
        val orphan = randomTool("orphan", metatoolCode = "missing-meta", isHidden = false)

        normalToolsFlow.value = listOf(orphan)
        assertEquals(emptyList(), producer.getFlow().first())
    }

    @Test
    fun `getFlow - default variant updates when metatool changes`() = runTest {
        val metaV1 = randomTool("meta", type = Tool.Type.META, defaultVariantCode = "v1")
        val metaV2 = randomTool("meta", type = Tool.Type.META, defaultVariantCode = "v2")
        val v1 = randomTool("v1", metatoolCode = "meta", isHidden = false)
        val v2 = randomTool("v2", metatoolCode = "meta", isHidden = false)

        producer.getFlow().test {
            normalToolsFlow.value = listOf(v1, v2)
            metatoolsFlow.value = listOf(metaV1)
            assertEquals(listOf(v1), expectMostRecentItem())

            metatoolsFlow.value = listOf(metaV2)
            assertEquals(listOf(v2), expectMostRecentItem())
        }
    }
    // endregion default variant filtering

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

    private fun createTool(
        category: String? = null,
        defaultOrder: Int = 0,
        isHidden: Boolean = false,
        metatoolCode: String? = null,
    ) = randomTool(
        category = category,
        defaultOrder = defaultOrder,
        isHidden = isHidden,
        metatoolCode = metatoolCode
    )
}
