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
import org.cru.godtools.model.randomTool

@Suppress("UnusedFlow")
class FeaturedToolsFlowProducerTest {
    private val countryFlow = MutableStateFlow<String?>("US")

    private val settings: Settings = mockk {
        every { getPersonalizationCountryFlow() } returns countryFlow
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getFeaturedToolsFlow(any(), any()) } returns flowOf(emptyList())
    }

    private val producer = FeaturedToolsFlowProducer(settings = settings, toolsRepository = toolsRepository)

    @Test
    fun `getFlow - uses getFeaturedToolsFlow`() = runTest {
        producer.getFlow(Locale.ENGLISH).first()
        verify { toolsRepository.getFeaturedToolsFlow(any(), any()) }
    }

    @Test
    fun `getFlow - uses the provided locale`() = runTest {
        producer.getFlow(Locale.FRENCH).first()
        verify { toolsRepository.getFeaturedToolsFlow(Locale.FRENCH, any()) }
        verify(exactly = 0) { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, any()) }
    }

    @Test
    fun `getFlow - uses Settings getPersonalizationCountryFlow for country`() = runTest {
        producer.getFlow(Locale.ENGLISH).first()
        verify { toolsRepository.getFeaturedToolsFlow(any(), "US") }
    }

    @Test
    fun `getFlow - returns featured tools when non-empty`() = runTest {
        val tool = createTool()
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(tool))

        assertEquals(listOf(tool), producer.getFlow(Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - GT-3101 - no featured tools when no country is selected`() = runTest {
        countryFlow.value = null

        assertEquals(emptyList(), producer.getFlow(Locale.ENGLISH).first())
        verify(exactly = 0) { toolsRepository.getFeaturedToolsFlow(any(), any()) }
    }

    @Test
    fun `getFlow - GT-3101 - no featured tools when none exist for the selected country`() = runTest {
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(emptyList())

        assertEquals(emptyList(), producer.getFlow(Locale.ENGLISH).first())
        verify(exactly = 0) { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, null) }
    }

    @Test
    fun `getFlow - excludes hidden tools`() = runTest {
        val hidden = createTool(isHidden = true)
        val visible = createTool(isHidden = false)
        every { toolsRepository.getFeaturedToolsFlow(any(), any()) } returns flowOf(listOf(hidden, visible))

        assertEquals(listOf(visible), producer.getFlow(Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - updates when country changes`() = runTest {
        val usTool = createTool()
        countryFlow.value = null
        every { toolsRepository.getFeaturedToolsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(usTool))

        producer.getFlow(Locale.ENGLISH).test {
            assertEquals(emptyList(), awaitItem())

            countryFlow.value = "US"
            assertEquals(listOf(usTool), awaitItem())
        }
    }

    private fun createTool(isHidden: Boolean = false) = randomTool(isHidden = isHidden)
}
