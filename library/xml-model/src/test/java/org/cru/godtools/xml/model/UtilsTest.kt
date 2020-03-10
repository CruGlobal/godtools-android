package org.cru.godtools.xml.model

import android.app.Application
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UtilsTest {
    @Test
    fun verifyParseColorOrNull() {
        assertThat("rgba(255,0,0,1)".parseColorOrNull(), equalTo(Color.RED))
        assertThat("rgba(0,255,0,1)".parseColorOrNull(), equalTo(Color.GREEN))
        assertThat("rgba(0,0,255,1)".parseColorOrNull(), equalTo(Color.BLUE))
        assertThat("rgba(0,0,0,1)".parseColorOrNull(), equalTo(Color.BLACK))
        assertThat("rgba(0,0,0,0)".parseColorOrNull(), equalTo(Color.TRANSPARENT))
        assertNull("akjsdf".parseColorOrNull())
    }

    @Test
    fun verifyParseUrl() {
        assertThat(parseUrl("https://example.com/path", null), equalTo(Uri.parse("https://example.com/path")))
        assertThat(parseUrl("www.example.com/path", null), equalTo(Uri.parse("http://www.example.com/path")))
        assertThat(parseUrl("mailto:someone@example.com", null), equalTo(Uri.parse("mailto:someone@example.com")))
    }
}
