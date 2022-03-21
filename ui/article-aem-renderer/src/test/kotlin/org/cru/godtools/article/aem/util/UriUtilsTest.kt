package org.cru.godtools.article.aem.util

import android.app.Application
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UriUtilsTest {
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
