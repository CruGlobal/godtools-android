package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Resource
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ToolsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: ToolsRepository
    abstract val attachmentsRepository: AttachmentsRepository

    // region findTool()
    @Test
    fun `findTool()`() = testScope.runTest {
        val tool = Tool("tool")
        repository.storeToolFromSync(tool)

        assertNull(repository.findTool("other"))
        assertThat(repository.findTool("tool"), tool(tool))
    }
    // endregion findTool()

    // region findResourceBlocking()
    @Test
    fun `findResourceBlocking()`() = testScope.runTest {
        val resource = Resource("tool")
        repository.storeToolFromSync(resource)

        assertNull(repository.findResourceBlocking("other"))
        assertThat(repository.findResourceBlocking("tool"), tool(resource))
    }
    // endregion findResourceBlocking()

    // region getResources()
    @Test
    fun `getResources() - Returns All Resource Types`() = testScope.runTest {
        val resources = Tool.Type.values().map { Resource(it.name.lowercase(), it) }
        repository.storeToolsFromSync(resources)

        assertThat(
            repository.getResources(),
            containsInAnyOrder(resources.map { tool(it) })
        )
    }
    // endregion getResources()

    // region getResourcesBlocking()
    @Test
    fun `getResourcesBlocking() - Returns All Resource Types`() = testScope.runTest {
        val resources = Tool.Type.values().map { Resource(it.name.lowercase(), it) }
        repository.storeToolsFromSync(resources)

        assertThat(
            repository.getResourcesBlocking(),
            containsInAnyOrder(resources.map { tool(it) })
        )
    }
    // endregion getResourcesBlocking()

    // region getTools()
    @Test
    fun `getTools() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.values().map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(tools)

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
        repository.storeToolsFromSync(listOf(hidden, visible))

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
        repository.storeToolsFromSync(listOf(meta, defaultVariant, otherVariant))

        assertThat(
            repository.getTools(),
            containsInAnyOrder(tool(defaultVariant), tool(otherVariant))
        )
    }
    // endregion getTools()

    // region getResourcesFlow()
    @Test
    fun `getResourcesFlow() - Returns All Resource Types`() = testScope.runTest {
        val resources = Tool.Type.values().map { Resource(it.name.lowercase(), it) }
        repository.storeToolsFromSync(resources)

        assertThat(
            repository.getResourcesFlow().first(),
            containsInAnyOrder(resources.map { tool(it) })
        )
    }
    // endregion getResourcesFlow()

    // region getToolsFlow()
    @Test
    fun `getToolsFlow() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.values().map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(tools)

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
        repository.storeToolsFromSync(listOf(hidden, visible))

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
        repository.storeToolsFromSync(listOf(meta, defaultVariant, otherVariant))

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
        repository.storeToolsFromSync(listOf(tool1, tool2, fav1, fav2))

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

            repository.storeToolsFromSync(listOf(meta1, meta2, tool1, tool2))
            runCurrent()
            assertThat(expectMostRecentItem(), containsInAnyOrder(tool(meta1), tool(meta2)))
        }
    }
    // endregion getMetaToolsFlow()

    // region getLessonsFlow()
    @Test
    fun `getLessonsFlow()`() = testScope.runTest {
        val tool = Tool("tool", type = Tool.Type.TRACT)
        val lesson = Tool("lesson", type = Tool.Type.LESSON)

        repository.getLessonsFlow().test {
            assertThat(awaitItem(), empty())

            repository.storeToolsFromSync(listOf(tool, lesson))
            runCurrent()
            assertThat(expectMostRecentItem(), contains(tool(lesson)))
        }
    }

    @Test
    fun `getLessonsFlow() - Don't filter hidden lessons`() = testScope.runTest {
        val hidden = Tool("hidden", type = Tool.Type.LESSON) { isHidden = true }
        val visible = Tool("visible", type = Tool.Type.LESSON) { isHidden = false }
        repository.storeToolsFromSync(listOf(hidden, visible))

        assertThat(
            repository.getLessonsFlow().first(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }
    // endregion getLessonsFlow()

    // region toolsChangeFlow()
    @Test
    fun `toolsChangeFlow(emitOnStart = true)`() = testScope.runTest {
        repository.toolsChangeFlow(emitOnStart = true).test {
            expectMostRecentItem()

            val tool = Tool().apply {
                id = 1
                code = "tool"
            }
            repository.storeInitialResources(listOf(tool))
            runCurrent()
            expectMostRecentItem()

            repository.pinTool("tool")
            runCurrent()
            expectMostRecentItem()
        }
    }

    @Test
    fun `toolsChangeFlow(emitOnStart = false)`() = testScope.runTest {
        repository.toolsChangeFlow(emitOnStart = false).test {
            runCurrent()
            expectNoEvents()
        }
    }
    // endregion toolsChangeFlow()

    @Test
    fun verifyPinTool() = testScope.runTest {
        val code = "pinTool"
        repository.storeToolFromSync(Tool(code))

        repository.findToolFlow(code).test {
            assertFalse(assertNotNull(awaitItem()).isAdded)

            repository.pinTool(code)
            assertTrue(assertNotNull(awaitItem()).isAdded)
        }
    }

    @Test
    fun verifyUnpinTool() = testScope.runTest {
        val code = "pinTool"
        repository.storeToolFromSync(Tool(code) { isAdded = true })

        repository.findToolFlow(code).test {
            assertTrue(assertNotNull(awaitItem()).isAdded)

            repository.unpinTool(code)
            assertFalse(assertNotNull(awaitItem()).isAdded)
        }
    }

    // region updateToolShares()
    @Test
    fun `updateToolViews()`() = testScope.runTest {
        val code = "shares"
        repository.storeToolFromSync(Tool(code))
        assertNotNull(repository.findTool(code)) { assertEquals(0, it.pendingShares) }

        repository.updateToolViews(code, 10)
        assertNotNull(repository.findTool(code)) { assertEquals(10, it.pendingShares) }

        repository.updateToolViews(code, -5)
        assertNotNull(repository.findTool(code)) { assertEquals(5, it.pendingShares) }
    }

    @Test
    fun `updateToolViews() - Concurrent Updates`() = testScope.runTest {
        val code = "shares"
        repository.storeToolFromSync(Tool(code))

        withContext(Dispatchers.IO) {
            repeat(1000) { launch { repository.updateToolViews(code, 1) } }
        }
        assertNotNull(repository.findTool(code)) { assertEquals(1000, it.pendingShares) }
    }
    // endregion updateToolShares()

    // region deleteBlocking()
    @Test
    fun `deleteBlocking()`() = testScope.runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        repository.storeToolsFromSync(listOf(tool1, tool2))
        assertThat(repository.getTools(), allOf(hasSize(2), containsInAnyOrder(tool(tool1), tool(tool2))))

        repository.deleteBlocking(tool1)
        assertThat(repository.getTools(), allOf(hasSize(1), contains(tool(tool2))))
    }

    @Test
    fun `deleteBlocking() - delete related attachments`() = testScope.runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        repository.storeToolsFromSync(listOf(tool1, tool2))
        attachmentsRepository.storeAttachmentsFromSync(
            listOf(
                Attachment().apply {
                    id = 1
                    toolId = tool1.id
                },
                Attachment().apply {
                    id = 2
                    toolId = tool1.id
                },
                Attachment().apply {
                    id = 3
                    toolId = tool2.id
                }
            )
        )
        assertThat(repository.getTools(), containsInAnyOrder(tool(tool1), tool(tool2)))
        assertEquals(setOf(1L, 2L, 3L), attachmentsRepository.getAttachments().map { it.id }.toSet())

        repository.deleteBlocking(tool1)
        assertThat(repository.getTools(), contains(tool(tool2)))
        assertEquals(setOf(3L), attachmentsRepository.getAttachments().map { it.id }.toSet())
    }
    // endregion deleteBlocking()

    // region Sync Methods
    // region storeToolFromSync()
    @Test
    fun `storeToolFromSync()`() = testScope.runTest {
        assertNull(repository.findTool("tool"))
        val tool = Tool("tool")

        repository.storeToolFromSync(tool)
        assertThat(repository.findTool("tool"), tool(tool))
    }

    @Test
    fun `storeToolFromSync() - Don't pave over added flag`() = testScope.runTest {
        val tool = Tool("tool") { isAdded = false }
        repository.storeToolFromSync(tool)
        repository.pinTool("tool")
        assertNotNull(repository.findTool("tool")) { assertTrue(it.isAdded) }

        repository.storeToolFromSync(tool)
        assertNotNull(repository.findTool("tool")) { assertTrue(it.isAdded) }
    }
    // endregion storeToolFromSync()
    // endregion Sync Methods
}
