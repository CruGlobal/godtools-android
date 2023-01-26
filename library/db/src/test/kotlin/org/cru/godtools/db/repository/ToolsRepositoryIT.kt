package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ToolsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: ToolsRepository

    // region findTool()
    @Test
    fun `findTool()`() = testScope.runTest {
        val tool = Tool("tool")
        repository.insert(tool)

        assertNull(repository.findTool("other"))
        assertThat(repository.findTool("tool"), tool(tool))
    }
    // endregion findTool()

    // region getTools()
    @Test
    fun `getTools() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.values().map { Tool(it.name.lowercase(), it) }
        repository.insert(*tools.toTypedArray())

        assertThat(
            repository.getTools(),
            containsInAnyOrder(
                tools
                    .filter { it.type == Tool.Type.ARTICLE || it.type == Tool.Type.CYOA || it.type == Tool.Type.TRACT }
                    .map { tool(it) }
            )
        )
    }

    @Test
    fun `getTools() - Don't filter hidden tools`() = testScope.runTest {
        val hidden = Tool("hidden") { isHidden = true }
        val visible = Tool("visible") { isHidden = false }
        repository.insert(hidden, visible)

        assertThat(
            repository.getTools(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }

    @Test
    fun `getTools() - Don't filter metatool variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META) { defaultVariantCode = "defaultVariant" }
        val defaultVariant = Tool("defaultVariant") { metatoolCode = "meta" }
        val otherVariant = Tool("otherVariant") { metatoolCode = "meta" }
        repository.insert(meta, defaultVariant, otherVariant)

        assertThat(
            repository.getTools(),
            containsInAnyOrder(tool(defaultVariant), tool(otherVariant))
        )
    }
    // endregion getTools()

    // region getToolsFlow()
    @Test
    fun `getToolsFlow() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.values().map { Tool(it.name.lowercase(), it) }
        repository.insert(*tools.toTypedArray())

        assertThat(
            repository.getToolsFlow().first(),
            containsInAnyOrder(
                tools
                    .filter { it.type == Tool.Type.ARTICLE || it.type == Tool.Type.CYOA || it.type == Tool.Type.TRACT }
                    .map { tool(it) }
            )
        )
    }

    @Test
    fun `getToolsFlow() - Don't filter hidden tools`() = testScope.runTest {
        val hidden = Tool("hidden") { isHidden = true }
        val visible = Tool("visible") { isHidden = false }
        repository.insert(hidden, visible)

        assertThat(
            repository.getToolsFlow().first(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }

    @Test
    fun `getToolsFlow() - Don't filter metatool variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META) { defaultVariantCode = "defaultVariant" }
        val defaultVariant = Tool("defaultVariant") { metatoolCode = "meta" }
        val otherVariant = Tool("otherVariant") { metatoolCode = "meta" }
        repository.insert(meta, defaultVariant, otherVariant)

        assertThat(
            repository.getToolsFlow().first(),
            containsInAnyOrder(tool(defaultVariant), tool(otherVariant))
        )
    }
    // endregion getToolsFlow()

    // region getFavoriteToolsFlow()
    @Test
    fun `getFavoriteToolsFlow()`() = testScope.runTest {
        val tool1 = Tool("tool1") { isAdded = false }
        val tool2 = Tool("tool2") { isAdded = false }
        val fav1 = Tool("fav1") { isAdded = true }
        val fav2 = Tool("fav2") { isAdded = true }
        repository.insert(tool1, tool2, fav1, fav2)

        assertThat(
            repository.getFavoriteToolsFlow().first(),
            containsInAnyOrder(tool(fav1), tool(fav2))
        )
    }
    // endregion getFavoriteToolsFlow()

    // region getMetaToolsFlow()
    @Test
    fun `getMetaToolsFlow()`() = testScope.runTest {
        val meta1 = Tool("meta1", Tool.Type.META)
        val meta2 = Tool("meta2", Tool.Type.META)
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        repository.getMetaToolsFlow().test {
            assertThat(awaitItem(), empty())

            repository.insert(meta1, meta2, tool1, tool2)
            runCurrent()
            assertThat(expectMostRecentItem(), containsInAnyOrder(tool(meta1), tool(meta2)))
        }
    }
    // endregion getMetaToolsFlow()

    @Test
    fun verifyPinTool() = testScope.runTest {
        val code = "pinTool"
        repository.insert(Tool(code))

        repository.findToolFlow(code).test {
            assertFalse(assertNotNull(awaitItem()).isAdded)

            repository.pinTool(code)
            assertTrue(assertNotNull(awaitItem()).isAdded)
        }
    }

    @Test
    fun verifyUnpinTool() = testScope.runTest {
        val code = "pinTool"
        repository.insert(Tool(code) { isAdded = true })

        repository.findToolFlow(code).test {
            assertTrue(assertNotNull(awaitItem()).isAdded)

            repository.unpinTool(code)
            assertFalse(assertNotNull(awaitItem()).isAdded)
        }
    }
}
