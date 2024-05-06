package org.cru.godtools.ui.languages

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import org.cru.godtools.R
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class I18nStringsTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val resources get() = context.resources

    @Test
    @Config(qualifiers = "ar")
    fun `Languages - Arabic - Valid format placeholders`() {
        // plurals
        repeat(200) {
            resources.getQuantityString(R.plurals.dashboard_tools_section_filter_available_tools, it, it)
            resources.getQuantityString(R.plurals.language_settings_downloadable_languages_available_tools, it, it)
            resources.getQuantityString(R.plurals.language_settings_section_app_language_available, it, it)
        }
    }
}
