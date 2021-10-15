package org.cru.godtools.tool.lesson.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.createLessonActivityIntent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class LessonActivityTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    // region Intent processing
    @Test
    fun `processIntent() - Valid direct`() {
        ActivityScenario.launch<LessonActivity>(context.createLessonActivityIntent(TOOL, Locale.ENGLISH)).use {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Invalid missing tool`() {
        val intent = context.createLessonActivityIntent(TOOL, Locale.ENGLISH)
        intent.removeExtra(EXTRA_TOOL)
        ActivityScenario.launch<LessonActivity>(intent).use {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Invalid missing locale`() {
        val intent = context.createLessonActivityIntent(TOOL, Locale.ENGLISH)
        intent.removeExtra(EXTRA_LANGUAGE)
        ActivityScenario.launch<LessonActivity>(intent).use {
            assertEquals(Lifecycle.State.DESTROYED, it.state)
        }
    }

    @Test
    fun `processIntent() - Valid deeplink`() {
        val intent = Intent(ACTION_VIEW, Uri.parse("https://godtoolsapp.com/lessons/test/en"))
        ActivityScenario.launch<LessonActivity>(intent).use {
            it.onActivity {
                assertEquals(TOOL, it.tool)
                assertEquals(Locale.ENGLISH, it.locale)
                assertFalse(it.isFinishing)
            }
        }
    }

    @Test
    fun `processIntent() - Invalid deeplinks`() {
        listOf(
            "https://example.com/lessons/test/en",
            "https://godtoolsapp.com/lessons/",
            "https://godtoolsapp.com/lessons/test",
        ).map { Intent(ACTION_VIEW, Uri.parse(it)) }.forEach {
            assertThrows(RuntimeException::class.java) { ActivityScenario.launch<LessonActivity>(it) }
        }

        // these match the intent-filter, but are still invalid
        listOf(
            "https://godtoolsapp.com/lessons/test/",
            "https://godtoolsapp.com/lessons//en",
        ).map { Intent(ACTION_VIEW, Uri.parse(it)) }.forEach {
            ActivityScenario.launch<LessonActivity>(it).use {
                assertEquals(Lifecycle.State.DESTROYED, it.state)
            }
        }
    }
    // endregion Intent processing
}
