package org.cru.godtools.xml.model

import android.app.Application
import android.graphics.Color
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class UtilsTest {
    @Test
    fun verifyParseColor() {
        assertThat(Utils.parseColor("rgba(255,0,0,1)", null), equalTo(Color.RED))
        assertThat(Utils.parseColor("rgba(0,255,0,1)", null), equalTo(Color.GREEN))
        assertThat(Utils.parseColor("rgba(0,0,255,1)", null), equalTo(Color.BLUE))
        assertThat(Utils.parseColor("rgba(0,0,0,1)", null), equalTo(Color.BLACK))

        // default parse behavior
        assertThat(Utils.parseColor(null, Color.RED), equalTo(Color.RED))
        assertThat(Utils.parseColor("akjsdf", Color.RED), equalTo(Color.RED))
    }

    @Test
    fun verifyParseUrl() {
        assertThat(Utils.parseUrl("https://example.com/path", null), equalTo(Uri.parse("https://example.com/path")))
        assertThat(Utils.parseUrl("www.example.com/path", null), equalTo(Uri.parse("http://www.example.com/path")))
        assertThat(Utils.parseUrl("mailto:someone@example.com", null), equalTo(Uri.parse("mailto:someone@example.com")))
    }
}
