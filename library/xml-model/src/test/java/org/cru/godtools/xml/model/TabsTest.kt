package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TabsTest {
    @Test
    fun testParseTabs() {
        val tabs = Tabs.fromXml(Manifest(), getXmlParserForResource("tabs.xml"))
        assertThat(tabs.tabs, hasSize(2))
    }
}
