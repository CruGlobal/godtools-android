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
        val manifest = parseManifestXml("manifest_empty.xml")
        assertThat(manifest.pages, empty())
        assertThat(manifest.mResources.size(), equalTo(0))
    }

    private fun parseManifestXml(file: String) =
        Manifest.fromXml(getXmlParserForResource(file), file, "kgp", Locale.ENGLISH)
}
