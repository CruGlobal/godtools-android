package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
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
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.ToolMatchers.tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.model.trackChanges
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ToolsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: ToolsRepository
    abstract val attachmentsRepository: AttachmentsRepository
    abstract val languagesRepository: LanguagesRepository
    abstract val translationsRepository: TranslationsRepository

    // region findTool()
    @Test
    fun `findTool()`() = testScope.runTest {
        val tool = Tool("tool")
        repository.storeToolsFromSync(setOf(tool))

        assertNull(repository.findTool("other"))
        assertThat(repository.findTool("tool"), tool(tool))
    }
    // endregion findTool()

    // region getResources()
    @Test
    fun `getResources() - Returns All Resource Types`() = testScope.runTest {
        val resources = Tool.Type.entries.map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(resources)

        assertThat(
            repository.getAllTools(),
            containsInAnyOrder(resources.map { tool(it) })
        )
    }
    // endregion getResources()

    // region getNormalTools()
    @Test
    fun `getNormalTools() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.entries.map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(tools)

        assertThat(
            repository.getNormalTools(),
            containsInAnyOrder(
                tools
                    .filter { it.type == Tool.Type.ARTICLE || it.type == Tool.Type.CYOA || it.type == Tool.Type.TRACT }
                    .map { tool(it) }
            )
        )
    }

    @Test
    fun `getNormalTools() - Don't filter hidden tools`() = testScope.runTest {
        val hidden = randomTool("hidden", Tool.Type.TRACT, isHidden = true)
        val visible = randomTool("visible", Tool.Type.TRACT, isHidden = false)
        repository.storeToolsFromSync(listOf(hidden, visible))

        assertThat(
            repository.getNormalTools(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }

    @Test
    fun `getNormalTools() - Don't filter metatool variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META, defaultVariantCode = "defaultVariant")
        val defaultVariant = Tool("defaultVariant", Tool.Type.TRACT, metatoolCode = "meta")
        val otherVariant = Tool("otherVariant", Tool.Type.TRACT, metatoolCode = "meta")
        repository.storeToolsFromSync(listOf(meta, defaultVariant, otherVariant))

        assertThat(
            repository.getNormalTools(),
            containsInAnyOrder(tool(defaultVariant), tool(otherVariant))
        )
    }
    // endregion getNormalTools()

    // region getAllToolsFlow()
    @Test
    fun `getAllToolsFlow() - Returns All Resource Types`() = testScope.runTest {
        val resources = Tool.Type.entries.map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(resources)

        assertThat(
            repository.getAllToolsFlow().first(),
            containsInAnyOrder(resources.map { tool(it) })
        )
    }
    // endregion getAllToolsFlow()

    // region getNormalToolsFlow()
    @Test
    fun `getNormalToolsFlow() - Supported Tool Types Only`() = testScope.runTest {
        val tools = Tool.Type.entries.map { Tool(it.name.lowercase(), it) }
        repository.storeToolsFromSync(tools)

        assertThat(
            repository.getNormalToolsFlow().first(),
            containsInAnyOrder(
                tools
                    .filter { it.type == Tool.Type.ARTICLE || it.type == Tool.Type.CYOA || it.type == Tool.Type.TRACT }
                    .map { tool(it) }
            )
        )
    }

    @Test
    fun `getNormalToolsFlow() - Don't filter hidden tools`() = testScope.runTest {
        val hidden = randomTool("hidden", Tool.Type.TRACT, isHidden = true)
        val visible = randomTool("visible", Tool.Type.TRACT, isHidden = false)
        repository.storeToolsFromSync(listOf(hidden, visible))

        assertThat(
            repository.getNormalToolsFlow().first(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }

    @Test
    fun `getNormalToolsFlow() - Don't filter metatool variants`() = testScope.runTest {
        val meta = Tool("meta", Tool.Type.META, defaultVariantCode = "defaultVariant")
        val defaultVariant = Tool("defaultVariant", Tool.Type.TRACT, metatoolCode = "meta")
        val otherVariant = Tool("otherVariant", Tool.Type.TRACT, metatoolCode = "meta")
        repository.storeToolsFromSync(listOf(meta, defaultVariant, otherVariant))

        assertThat(
            repository.getNormalToolsFlow().first(),
            containsInAnyOrder(tool(defaultVariant), tool(otherVariant))
        )
    }
    // endregion getNormalToolsFlow()

    // region getToolsFlowForLanguage()
    @Test
    fun `getToolsFlowForLanguage()`() = testScope.runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        repository.storeInitialTools(listOf(tool1, tool2))
        languagesRepository.storeInitialLanguages(listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH)))

        repository.getToolsFlowForLanguage(Locale.ENGLISH).test {
            assertTrue(awaitItem().isEmpty())

            translationsRepository.storeTranslationFromSync(randomTranslation("tool1", Locale.ENGLISH))
            assertThat(awaitItem(), contains(tool(tool1)))

            translationsRepository.storeTranslationFromSync(randomTranslation("tool2", Locale.FRENCH))
            assertThat(awaitItem(), contains(tool(tool1)))

            translationsRepository.storeTranslationFromSync(randomTranslation("tool2", Locale.ENGLISH))
            assertThat(awaitItem(), containsInAnyOrder(tool(tool1), tool(tool2)))
        }
    }
    // endregion getToolsFlowForLanguage()

    // region getDownloadedToolsFlowByTypesAndLanguage()
    @Test
    fun `getDownloadedToolsFlowByTypesAndLanguage()`() = testScope.runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        repository.storeInitialTools(listOf(tool1, tool2))
        languagesRepository.storeInitialLanguages(listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH)))

        repository.getDownloadedToolsFlowByTypesAndLanguage(Tool.Type.NORMAL_TYPES, Locale.ENGLISH).test {
            assertTrue(awaitItem().isEmpty())

            translationsRepository.storeInitialTranslations(
                listOf(
                    randomTranslation("tool1", Locale.ENGLISH, isDownloaded = true),
                    randomTranslation("tool2", Locale.ENGLISH, isDownloaded = false),
                    randomTranslation("tool2", Locale.FRENCH, isDownloaded = true),
                )
            )
            assertThat(awaitItem(), contains(tool(tool1)))
        }
    }
    // endregion getDownloadedToolsFlowByTypesAndLanguage()

    // region getFavoriteToolsFlow()
    @Test
    fun `getFavoriteToolsFlow()`() = testScope.runTest {
        val tool1 = Tool("tool1", Tool.Type.TRACT, isFavorite = false)
        val tool2 = Tool("tool2", Tool.Type.TRACT, isFavorite = false)
        val fav1 = Tool("fav1", Tool.Type.TRACT, isFavorite = true)
        val fav2 = Tool("fav2", Tool.Type.TRACT, isFavorite = true)
        repository.storeInitialTools(listOf(tool1, tool2, fav1, fav2))

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
        val hidden = randomTool("hidden", Tool.Type.LESSON, isHidden = true)
        val visible = randomTool("visible", Tool.Type.LESSON, isHidden = false)
        repository.storeToolsFromSync(listOf(hidden, visible))

        assertThat(
            repository.getLessonsFlow().first(),
            containsInAnyOrder(tool(hidden), tool(visible))
        )
    }
    // endregion getLessonsFlow()

    // region toolsChangeFlow()
    @Test
    fun `toolsChangeFlow()`() = testScope.runTest {
        repository.toolsChangeFlow().test {
            runCurrent()
            expectMostRecentItem()

            val tool = Tool("tool", apiId = 1)
            repository.storeInitialTools(listOf(tool))
            runCurrent()
            expectMostRecentItem()

            repository.pinTool("tool")
            runCurrent()
            expectMostRecentItem()
        }
    }
    // endregion toolsChangeFlow()

    // region pinTool()
    @Test
    fun `pinTool()`() = testScope.runTest {
        val code = "pinTool"
        repository.storeInitialTools(listOf(Tool(code)))

        repository.findToolFlow(code).test {
            assertNotNull(awaitItem()) {
                assertFalse(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }

            repository.pinTool(code)
            assertNotNull(awaitItem()) {
                assertTrue(it.isFavorite)
                assertTrue(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }
        }
    }

    @Test
    fun `pinTool(trackChanges = false)`() = testScope.runTest {
        val code = "pinTool"
        repository.storeInitialTools(listOf(Tool(code)))

        repository.findToolFlow(code).test {
            assertNotNull(awaitItem()) {
                assertFalse(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }

            repository.pinTool(code, trackChanges = false)
            assertNotNull(awaitItem()) {
                assertTrue(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }
        }
    }

    @Test
    fun `pinTool() - No Change`() = testScope.runTest {
        val code = "pinTool"
        repository.storeInitialTools(listOf(Tool(code, isFavorite = true)))

        repository.findToolFlow(code).test {
            assertNotNull(awaitItem()) {
                assertTrue(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }

            repository.pinTool(code)
            assertNotNull(awaitItem()) {
                assertTrue(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }
        }
    }
    // endregion pinTool()

    // region unpinTool()
    @Test
    fun `unpinTool()`() = testScope.runTest {
        val code = "unpinTool"
        repository.storeInitialTools(listOf(Tool(code, isFavorite = true)))

        repository.findToolFlow(code).test {
            assertNotNull(awaitItem()) {
                assertTrue(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }

            repository.unpinTool(code)
            assertNotNull(awaitItem()) {
                assertFalse(it.isFavorite)
                assertTrue(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }
        }
    }

    @Test
    fun `unpinTool() - No Change`() = testScope.runTest {
        val code = "unpinTool"
        repository.storeInitialTools(listOf(Tool(code, isFavorite = false)))

        repository.findToolFlow(code).test {
            assertNotNull(awaitItem()) {
                assertFalse(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }

            repository.unpinTool(code)
            assertNotNull(awaitItem()) {
                assertFalse(it.isFavorite)
                assertFalse(Tool.ATTR_IS_FAVORITE in it.changedFields)
            }
        }
    }
    // endregion unpinTool()

    // region storeToolOrder()
    @Test
    fun `storeToolOrder()`() = testScope.runTest {
        val tool1 = Tool("tool1", Tool.Type.TRACT, order = 7)
        val tool2 = Tool("tool2", Tool.Type.TRACT, order = 6)
        val tool3 = Tool("tool3", Tool.Type.TRACT, order = 5)
        repository.storeInitialTools(listOf(tool1, tool2, tool3))
        assertEquals(
            listOf("tool3", "tool2", "tool1"),
            repository.getNormalTools().sortedBy { it.order }.map { it.code }
        )

        repository.storeToolOrder(listOf("tool1", "tool3"))
        assertEquals(
            listOf("tool1", "tool3", "tool2"),
            repository.getNormalTools().sortedBy { it.order }.map { it.code }
        )
    }
    // endregion storeToolOrder()

    // region updateToolShares()
    @Test
    fun `updateToolViews()`() = testScope.runTest {
        val code = "shares"
        repository.storeToolsFromSync(setOf(Tool(code)))
        assertNotNull(repository.findTool(code)) { assertEquals(0, it.pendingShares) }

        repository.updateToolViews(code, 10)
        assertNotNull(repository.findTool(code)) { assertEquals(10, it.pendingShares) }

        repository.updateToolViews(code, -5)
        assertNotNull(repository.findTool(code)) { assertEquals(5, it.pendingShares) }
    }

    @Test
    fun `updateToolViews() - Concurrent Updates`() = testScope.runTest {
        val code = "shares"
        repository.storeToolsFromSync(setOf(Tool(code)))

        withContext(Dispatchers.IO) {
            repeat(1000) { launch { repository.updateToolViews(code, 1) } }
        }
        assertNotNull(repository.findTool(code)) { assertEquals(1000, it.pendingShares) }
    }
    // endregion updateToolShares()

    // region storeToolsFromSync()
    @Test
    fun `storeToolsFromSync() - New Tool`() = testScope.runTest {
        assertNull(repository.findTool("tool"))
        val tool = randomTool("tool", Tool.Type.LESSON)

        repository.storeToolsFromSync(setOf(tool))
        assertNotNull(repository.findTool("tool")) {
            assertEquals(tool.code, it.code)
            assertEquals(tool.type, it.type)
            assertEquals(tool.name, it.name)
            assertEquals(tool.category, it.category)
            assertEquals(tool.description, it.description)
            assertEquals(tool.shares, it.shares)
            assertEquals(tool.bannerId, it.bannerId)
            assertEquals(tool.detailsBannerId, it.detailsBannerId)
            assertEquals(tool.detailsBannerAnimationId, it.detailsBannerAnimationId)
            assertEquals(tool.detailsBannerYoutubeVideoId, it.detailsBannerYoutubeVideoId)
            assertEquals(tool.isScreenShareDisabled, it.isScreenShareDisabled)
            assertEquals(tool.defaultOrder, it.defaultOrder)
            assertEquals(tool.metatoolCode, it.metatoolCode)
            assertEquals(tool.defaultVariantCode, it.defaultVariantCode)
            assertEquals(tool.isHidden, it.isHidden)
            assertEquals(tool.isSpotlight, it.isSpotlight)
            assertEquals(tool.apiId, it.apiId)
        }
    }

    @Test
    fun `storeToolsFromSync() - Update Tool`() = testScope.runTest {
        val initial = randomTool("tool", Tool.Type.TRACT)
        repository.storeToolsFromSync(setOf(initial))
        val updated = randomTool("tool", Tool.Type.LESSON)

        repository.storeToolsFromSync(setOf(updated))
        assertNotNull(repository.findTool("tool")) {
            assertEquals(updated.code, it.code)
            assertEquals(updated.type, it.type)
            assertEquals(updated.name, it.name)
            assertEquals(updated.category, it.category)
            assertEquals(updated.description, it.description)
            assertEquals(updated.shares, it.shares)
            assertEquals(updated.bannerId, it.bannerId)
            assertEquals(updated.detailsBannerId, it.detailsBannerId)
            assertEquals(updated.detailsBannerAnimationId, it.detailsBannerAnimationId)
            assertEquals(updated.detailsBannerYoutubeVideoId, it.detailsBannerYoutubeVideoId)
            assertEquals(updated.isScreenShareDisabled, it.isScreenShareDisabled)
            assertEquals(updated.defaultOrder, it.defaultOrder)
            assertEquals(updated.metatoolCode, it.metatoolCode)
            assertEquals(updated.defaultVariantCode, it.defaultVariantCode)
            assertEquals(updated.isHidden, it.isHidden)
            assertEquals(updated.isSpotlight, it.isSpotlight)
            assertEquals(updated.apiId, it.apiId)
        }
    }

    @Test
    fun `storeToolsFromSync() - Don't pave over added flag`() = testScope.runTest {
        val tool = Tool("tool", isFavorite = false)
        repository.storeToolsFromSync(setOf(tool))
        repository.pinTool("tool")
        assertNotNull(repository.findTool("tool")) { assertTrue(it.isFavorite) }

        repository.storeToolsFromSync(setOf(tool))
        assertNotNull(repository.findTool("tool")) { assertTrue(it.isFavorite) }
    }

    @Test
    fun `storeToolsFromSync() - Don't pave over pending tool views`() = testScope.runTest {
        val tool = Tool("tool", pendingShares = 0)
        repository.storeToolsFromSync(setOf(tool))
        assertNotNull(repository.findTool("tool")) { assertEquals(0, it.pendingShares) }
        repository.updateToolViews("tool", 5)
        assertNotNull(repository.findTool("tool")) { assertEquals(5, it.pendingShares) }

        repository.storeToolsFromSync(setOf(tool))
        assertNotNull(repository.findTool("tool")) { assertEquals(5, it.pendingShares) }
    }
    // endregion storeToolsFromSync()

    // region storeFavoriteToolsFromSync()
    @Test
    fun `storeFavoriteToolsFromSync()`() = testScope.runTest {
        repository.storeInitialTools(
            listOf(
                Tool("tool1", isFavorite = true),
                Tool("tool2", isFavorite = false),
                Tool("tool3", isFavorite = true),
            )
        )

        repository.storeFavoriteToolsFromSync(listOf(Tool("tool2"), Tool("tool3")))
        assertFalse(repository.findTool("tool1")!!.isFavorite)
        assertTrue(repository.findTool("tool2")!!.isFavorite)
        assertTrue(repository.findTool("tool3")!!.isFavorite)
    }

    @Test
    fun `storeFavoriteToolsFromSync() - Handle dirty tools`() = testScope.runTest {
        repository.storeInitialTools(
            listOf(
                Tool("tool1", isFavorite = true).apply { trackChanges { markChanged(Tool.ATTR_IS_FAVORITE) } },
                Tool("tool2", isFavorite = false).apply { trackChanges { markChanged(Tool.ATTR_IS_FAVORITE) } },
                Tool("tool3", isFavorite = true).apply { trackChanges { markChanged(Tool.ATTR_IS_FAVORITE) } },
            )
        )

        repository.storeFavoriteToolsFromSync(listOf(Tool("tool2"), Tool("tool3")))
        assertNotNull(repository.findTool("tool1")) {
            assertTrue(it.isFavorite)
            assertTrue(it.isFieldChanged(Tool.ATTR_IS_FAVORITE))
        }
        assertNotNull(repository.findTool("tool2")) {
            assertFalse(it.isFavorite)
            assertTrue(it.isFieldChanged(Tool.ATTR_IS_FAVORITE))
        }
        assertNotNull(repository.findTool("tool3")) {
            assertTrue(it.isFavorite)
            assertFalse(it.isFieldChanged(Tool.ATTR_IS_FAVORITE))
        }
    }
    // endregion storeFavoriteToolsFromSync()

    // region deleteIfNotFavorite()
    @Test
    fun `deleteIfNotFavorite()`() = testScope.runTest {
        repository.storeToolsFromSync(setOf(Tool("tool")))
        assertNotNull(repository.findTool("tool"))

        repository.deleteIfNotFavorite("tool")
        assertNull(repository.findTool("tool"))
    }

    @Test
    fun `deleteIfNotFavorite() - Delete related Attachments`() = testScope.runTest {
        val tool1 = Tool("tool1")
        val tool2 = Tool("tool2")
        val tool3 = Tool("tool3", isFavorite = true)
        val attachment1 = Attachment(tool = tool1)
        val attachment2 = Attachment(tool = tool2)
        val attachment3 = Attachment(tool = tool3)
        repository.storeInitialTools(listOf(tool1, tool2, tool3))
        attachmentsRepository.storeInitialAttachments(listOf(attachment1, attachment2, attachment3))
        assertNotNull(attachmentsRepository.findAttachment(attachment1.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment2.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment3.id))

        repository.deleteIfNotFavorite("tool1")
        assertNull(attachmentsRepository.findAttachment(attachment1.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment2.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment3.id))

        // tool 3 is favorited, so it shouldn't be deleted or any associated attachments
        repository.deleteIfNotFavorite("tool3")
        assertNull(attachmentsRepository.findAttachment(attachment1.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment2.id))
        assertNotNull(attachmentsRepository.findAttachment(attachment3.id))
    }

    @Test
    fun `deleteIfNotFavorite() - Don't delete favorited tools`() = testScope.runTest {
        repository.storeInitialTools(listOf(Tool("tool", isFavorite = true)))
        assertNotNull(repository.findTool("tool"))

        repository.deleteIfNotFavorite("tool")
        assertNotNull(repository.findTool("tool"))
    }
    // endregion deleteIfNotFavorite()
}
