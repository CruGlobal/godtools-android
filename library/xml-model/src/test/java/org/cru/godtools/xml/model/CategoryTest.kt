package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class CategoryTest {
    @Test
    fun testParseCategory() {
        val manifest = Manifest(TOOL_CODE, Locale.US, getXmlParserForResource("categories.xml")) { TODO() }
        val category = manifest.findCategory("testParseCategory")!!
        assertEquals("testParseCategory", category.id)
        val banner = category.banner!!
        assertEquals("banner.jpg", banner.name)
        assertEquals("bannersha1.jpg", banner.localName)
        assertThat(category.aemTags, containsInAnyOrder("tag1", "tag2"))
        val label = category.label!!
        assertEquals("Category", label.text)
    }
}
