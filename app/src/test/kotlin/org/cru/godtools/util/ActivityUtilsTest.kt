package org.cru.godtools.util

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertNull
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ActivityUtilsTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `createToolIntent() - Invalid - code=null`() {
        assertNull(Tool(code = null).createToolIntent(context, listOf(Locale.ENGLISH)))
    }

    @Test
    fun `createToolIntent() - Invalid - no languages`() {
        assertNull(randomTool().createToolIntent(context, emptyList()))
    }
}
