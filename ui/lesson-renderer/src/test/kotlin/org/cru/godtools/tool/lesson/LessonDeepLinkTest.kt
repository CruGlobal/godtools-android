package org.cru.godtools.tool.lesson

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessonDeepLinkTest {
    // region parseKnowGodDeepLink()
    @Test
    fun `parseKnowGodDeepLink() - Base`() {
        assertNotNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/lesson/lessonhs"))) {
            assertEquals("lessonhs", it.lesson)
            assertEquals(Locale.forLanguageTag("en"), it.locale)
            assertNull(it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - With Page Number`() {
        assertNotNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/lesson/lessonhs/1"))) {
            assertEquals("lessonhs", it.lesson)
            assertEquals(Locale.forLanguageTag("en"), it.locale)
            assertEquals(1, it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - Non-numeric Page`() {
        assertNotNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/lesson/lessonhs/one"))) {
            assertEquals("lessonhs", it.lesson)
            assertEquals(Locale.forLanguageTag("en"), it.locale)
            // non-numeric page should result in null page
            assertNull(it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - Invalid`() {
        // too short
        assertNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en")))
        // missing 'lesson' segment
        assertNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/lessonhs")))
        // wrong host
        assertNull(LessonDeepLink.parseKnowGodDeepLink(Uri.parse("https://godtoolsapp.com/en/lesson/lessonhs")))
    }
    // endregion parseKnowGodDeepLink()
}
