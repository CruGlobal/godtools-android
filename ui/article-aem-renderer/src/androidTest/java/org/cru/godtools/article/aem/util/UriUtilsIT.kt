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

    @Test
    fun testUriRemoveExtension() {
        assertEquals("http://domain/a", Uri.parse("http://domain/a.txt").removeExtension().toString())
        assertEquals("http://domain/a", Uri.parse("http://domain/a.1.txt").removeExtension().toString())
        assertEquals("http://domain/a?p=v", Uri.parse("http://domain/a.1.txt?p=v").removeExtension().toString())
        assertEquals("http://domain/a.fld/b", Uri.parse("http://domain/a.fld/b.txt").removeExtension().toString())
    }
}
