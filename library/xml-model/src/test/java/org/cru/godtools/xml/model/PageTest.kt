package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageTest {
    private lateinit var manifest: Manifest

    @Before
    fun setup() {
        manifest = Manifest(TOOL_CODE)
    }

    @Test
    fun verifyParseModals() {
        val page = parsePageXml("page_modals.xml")
        assertThat(page.modals, hasSize(2))
        assertEquals("Modal 1", page.modals[0].title!!.text)
        assertEquals("Modal 2", page.modals[1].title!!.text)
    }

    private fun parsePageXml(file: String) =
        Page(manifest, 0).apply { parsePageXml(getXmlParserForResource(file)) }
}
