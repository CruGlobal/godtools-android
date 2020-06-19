package org.cru.godtools.xml.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.util.getXmlParserForResource
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ManifestTest {
    @Test
    fun verifyParseEmptyManifest() {
        val manifest = Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_empty.xml")) { TODO() }
        assertThat(manifest.pages, empty())
        assertThat(manifest.resources.size, equalTo(0))
    }
}
