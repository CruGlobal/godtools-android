package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ToolsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: ToolsRepository

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
