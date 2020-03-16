package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabTest {
    private lateinit var tabs: Tabs

    @Before
    fun setup() {
        tabs = Tabs(Manifest(), emptyList())
    }

    @Test
    fun testParseTab() {
        val tab = Tab(tabs, 5, getXmlParserForResource("tab.xml"))
        assertEquals(5, tab.position)
        assertThat(tab.analyticsEvents, hasSize(1))
        assertEquals("Tab 1", tab.label!!.text)
        assertThat(
            tab.content,
            contains(instanceOf(Image::class.java), instanceOf(Paragraph::class.java), instanceOf(Tabs::class.java))
        )
    }

    @Test
    fun testParseTabIgnoredContent() {
        val tab = Tab(tabs, 0, getXmlParserForResource("tab_ignored_content.xml"))
        assertThat(tab.content, contains(instanceOf(Paragraph::class.java), instanceOf(Tabs::class.java)))
    }
}
