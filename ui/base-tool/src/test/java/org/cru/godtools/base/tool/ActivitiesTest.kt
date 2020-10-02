package org.cru.godtools.base.tool

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.spy
import java.util.Locale
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.tract.activity.TractActivity
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayContaining
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

private const val TOOL = "tool"

@RunWith(AndroidJUnit4::class)
class ActivitiesTest {
    private lateinit var activity: Activity

    @Before
    fun setup() {
        activity = spy(Robolectric.buildActivity(AppCompatActivity::class.java).get())
    }

    @Test
    fun verifyCreateTractActivityIntent() {
        val intent = activity.createTractActivityIntent(TOOL, Locale.ENGLISH, null, Locale.FRENCH, Locale.CANADA)
        intent.assertTractIntent(languages = arrayOf(Locale.ENGLISH, Locale.FRENCH, Locale.CANADA))
    }

    @Test
    fun verifyCreateTractActivityIntentWithShowTips() {
        val intent = activity.createTractActivityIntent(TOOL, Locale.ENGLISH, Locale.FRENCH, showTips = true)
        intent.assertTractIntent(languages = arrayOf(Locale.ENGLISH, Locale.FRENCH), showTips = true)
    }

    private fun Intent.assertTractIntent(tool: String = TOOL, vararg languages: Locale, showTips: Boolean = false) {
        assertEquals(ComponentName(activity, TractActivity::class.java), component)
        assertEquals(tool, getStringExtra(EXTRA_TOOL))
        assertThat(extras!!.getLocaleArray(EXTRA_LANGUAGES)!!, arrayContaining(*languages))
        assertEquals(showTips, getBooleanExtra(EXTRA_SHOW_TIPS, !showTips))
    }
}
