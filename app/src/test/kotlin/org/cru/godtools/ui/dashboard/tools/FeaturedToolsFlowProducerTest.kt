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
class FeaturedToolsFlowProducerTest {
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val countryFlow = MutableStateFlow<String?>(null)
    private val normalToolsFlow = MutableStateFlow(emptyList<Tool>())

    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@FeaturedToolsFlowProducerTest.appLanguageFlow
        every { getPersonalizationCountryFlow() } returns countryFlow
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getNormalToolsFlow() } returns normalToolsFlow
        every { getFeaturedToolsFlow(any(), any()) } returns flowOf(emptyList())
    }

    private val producer = FeaturedToolsFlowProducer(settings = settings, toolsRepository = toolsRepository)

    // region ALL_TOOLS mode
    @Test
    fun `getFlow - All Tools - uses getNormalToolsFlow`() = runTest {
        producer.getFlow(mode = Mode.ALL_TOOLS).first()
        verify { toolsRepository.getNormalToolsFlow() }
        verify(exactly = 0) { toolsRepository.getFeaturedToolsFlow(any(), any()) }
    }

    @Test
    fun `getFlow - All Tools - only returns spotlight tools`() = runTest {
        val spotlight = createTool(isSpotlight = true)
        val nonSpotlight = createTool(isSpotlight = false)

        normalToolsFlow.value = listOf(spotlight, nonSpotlight)
        assertEquals(listOf(spotlight), producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }

    @Test
    fun `getFlow - All Tools - excludes hidden tools`() = runTest {
        val hidden = createTool(isSpotlight = true, isHidden = true)
        val visible = createTool(isSpotlight = true, isHidden = false)

        normalToolsFlow.value = listOf(hidden, visible)
        assertEquals(listOf(visible), producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }

    @Test
    fun `getFlow - All Tools - spotlight tools sorted by defaultOrder`() = runTest {
        val tools = List(5) { createTool(isSpotlight = true, defaultOrder = it) }

        normalToolsFlow.value = tools.shuffled()
        assertEquals(tools, producer.getFlow(mode = Mode.ALL_TOOLS).first())
    }
    // endregion ALL_TOOLS mode

    // region PERSONALIZATION mode
    @Test
    fun `getFlow - Personalization - uses getFeaturedToolsFlow`() = runTest {
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getFeaturedToolsFlow(any(), any()) }
        verify(exactly = 0) { toolsRepository.getNormalToolsFlow() }
    }

    @Test
    fun `getFlow - Personalization - uses appLanguageFlow when no language provided`() = runTest {
        appLanguageFlow.value = Locale.FRENCH
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getFeaturedToolsFlow(Locale.FRENCH, any()) }
    }

    @Test
    fun `getFlow - Personalization - uses provided language`() = runTest {
        producer.getFlow(mode = Mode.PERSONALIZATION, language = Locale.GERMAN).first()
        verify { toolsRepository.getFeaturedToolsFlow(Locale.GERMAN, any()) }
        verify(exactly = 0) { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, any()) }
    }

    @Test
    fun `getFlow - Personalization - uses Settings getPersonalizationCountryFlow for country`() = runTest {
        countryFlow.value = "US"
        producer.getFlow(mode = Mode.PERSONALIZATION).first()
        verify { toolsRepository.getFeaturedToolsFlow(any(), "US") }
    }

    @Test
    fun `getFlow - Personalization - returns featured tools when non-empty`() = runTest {
        val tool = createTool()
        val fallbackTool = createTool()
        countryFlow.value = "US"
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(tool))
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallbackTool))

        assertEquals(listOf(tool), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization - falls back to language only when no country-specific tools`() = runTest {
        val fallbackTool = createTool()
        countryFlow.value = "US"
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(emptyList())
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallbackTool))

        assertEquals(listOf(fallbackTool), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization - excludes hidden tools`() = runTest {
        val hidden = createTool(isHidden = true)
        val visible = createTool(isHidden = false)
        every { toolsRepository.getFeaturedToolsFlow(any(), any()) } returns flowOf(listOf(hidden, visible))

        assertEquals(listOf(visible), producer.getFlow(mode = Mode.PERSONALIZATION).first())
    }

    @Test
    fun `getFlow - Personalization - updates when appLanguage changes`() = runTest {
        val frenchTool = createTool()
        every { toolsRepository.getFeaturedToolsFlow(Locale.FRENCH, null) } returns flowOf(listOf(frenchTool))

        producer.getFlow(mode = Mode.PERSONALIZATION).test {
            assertEquals(emptyList(), awaitItem())

            appLanguageFlow.value = Locale.FRENCH
            assertEquals(listOf(frenchTool), awaitItem())
        }
    }

    @Test
    fun `getFlow - Personalization - updates when country changes`() = runTest {
        val usTool = createTool()
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(usTool))

        producer.getFlow(mode = Mode.PERSONALIZATION).test {
            assertEquals(emptyList(), awaitItem())

            countryFlow.value = "US"
            assertEquals(listOf(usTool), awaitItem())
        }
    }
    // endregion PERSONALIZATION mode

    private fun createTool(defaultOrder: Int = 0, isHidden: Boolean = false, isSpotlight: Boolean = false) =
        randomTool(defaultOrder = defaultOrder, isHidden = isHidden, isSpotlight = isSpotlight)
}
