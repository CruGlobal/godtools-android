package org.cru.godtools.article.aem.util

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test

class UriUtilsIT {
    @Test
    fun testUriAddExtension() {
        assertEquals("https://domain/a.txt", Uri.parse("https://domain/a").addExtension("txt").toString())
        assertEquals("https://domain/.txt", Uri.parse("https://domain").addExtension("txt").toString())
        assertEquals("https://domain/a.txt?p=v", Uri.parse("https://domain/a?p=v").addExtension("txt").toString())
    }
}
