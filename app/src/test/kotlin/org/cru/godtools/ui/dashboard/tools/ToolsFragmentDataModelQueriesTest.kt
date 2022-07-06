package org.cru.godtools.ui.dashboard.tools

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import javax.inject.Inject
import kotlin.random.Random
import org.cru.godtools.model.Tool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasProperty
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class ToolsFragmentDataModelQueriesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dao: GodToolsDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // region QUERY_TOOLS
    @Test
    fun `QUERY_TOOLS - return only supported types`() {
        val tract = createTool("tract") { type = Tool.Type.TRACT }
        val cyoa = createTool("cyoa") { type = Tool.Type.CYOA }
        val article = createTool("article") { type = Tool.Type.ARTICLE }
        val lesson = createTool("lesson") { type = Tool.Type.LESSON }
        val meta = createTool("meta") { type = Tool.Type.META }

        val tools = dao.get(QUERY_TOOLS)
        assertThat(
            tools,
            allOf(
                containsInAnyOrder(tool(tract), tool(cyoa), tool(article)),
                not(hasItem(tool(lesson))),
                not(hasItem(tool(meta)))
            )
        )
    }

    @Test
    fun `QUERY_TOOLS - return only default variants`() {
        val meta = createTool("meta") {
            type = Tool.Type.META
            defaultVariantCode = "variant2"
        }
        val variant1 = createTool("variant1") { metatoolCode = "meta" }
        val variant2 = createTool("variant2") { metatoolCode = "meta" }

        val tools = dao.get(QUERY_TOOLS)
        assertThat(tools, allOf(contains(tool(variant2)), not(hasItem(tool(meta))), not(hasItem(tool(variant1)))))
    }
    // endregion QUERY_TOOLS

    // region QUERY_TOOLS_SPOTLIGHT
    @Test
    fun `QUERY_TOOLS_SPOTLIGHT - returns only spotlighted tools`() {
        val normal = createTool("normal")
        val spotlight = createTool("spotlight") { isSpotlight = true }
        val meta = createTool("meta") {
            type = Tool.Type.META
            defaultVariantCode = "normalVariant"
        }
        val spotlightVariant = createTool("spotlightVariant") {
            metatoolCode = "meta"
            isSpotlight = true
        }
        val normalVariant = createTool("normalVariant") { metatoolCode = "meta" }

        val tools = dao.get(QUERY_TOOLS_SPOTLIGHT)
        assertThat(
            tools,
            allOf(
                containsInAnyOrder(tool(spotlight), tool(spotlightVariant)),
                not(hasItem(tool(normal))),
                not(hasItem(tool(meta))),
                not(hasItem(tool(normalVariant))),
            )
        )
    }
    // endregion QUERY_TOOLS_SPOTLIGHT

    private fun createTool(code: String = "tool", config: Tool.() -> Unit = {}) = Tool().apply {
        id = Random.nextLong()
        this.code = code
        type = Tool.Type.TRACT
        config()
    }.also { dao.insert(it) }

    private fun tool(tool: Tool) = allOf<Tool>(
        hasProperty("id", equalTo(tool.id)),
        hasProperty("code", equalTo(tool.code))
    )
}
