package org.cru.godtools.ui.dashboard

import android.app.Application
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DashboardDeepLinksTest {
    @Test
    fun testIsDashboardLessonDeepLink() {
        assertTrue(Uri.parse("http://godtoolsapp.com/lessons").isDashboardLessonsDeepLink())
        assertTrue(Uri.parse("https://godtoolsapp.com/lessons").isDashboardLessonsDeepLink())
        assertFalse(Uri.parse("https://godtoolsapp.com/lessons/lessonhs/en").isDashboardLessonsDeepLink())
        assertFalse(Uri.parse("https://godtoolsapp.com/lessons/").isDashboardLessonsDeepLink())
        assertFalse(Uri.parse("https://godtoolsapp.com/lesson").isDashboardLessonsDeepLink())
        assertFalse(Uri.parse("https://knowgod.com/lessons").isDashboardLessonsDeepLink())
    }
}
