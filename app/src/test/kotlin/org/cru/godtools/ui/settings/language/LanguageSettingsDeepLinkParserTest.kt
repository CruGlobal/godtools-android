package org.cru.godtools.ui.settings.language

import android.app.Application
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.cru.godtools.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.ui.settings.language.LanguageSettingsDeepLinkParser.isDeepLinkSupported
import org.cru.godtools.ui.settings.language.LanguageSettingsDeepLinkParser.parseDeepLink
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LanguageSettingsDeepLinkParserTest {
    // region isDeepLinkSupported()
    @Test
    fun `isDeepLinkSupported() - Valid`() {
        assertTrue(isDeepLinkSupported(Uri.parse("http://godtoolsapp.com/deeplink/settings/language")))
        assertTrue(isDeepLinkSupported(Uri.parse("https://godtoolsapp.com/deeplink/settings/language")))
        assertTrue(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/settings/language")))
    }

    @Test
    fun `isDeepLinkSupported() - Invalid`() {
        assertFalse(isDeepLinkSupported(Uri.parse("ftp://godtoolsapp.com/deeplink/settings/language")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://example.com/deeplink/settings/language")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://godtoolsapp.com/settings/language")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://godtoolsapp.com/deeplink/settings")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://godtoolsapp.com/deeplink/settings/language/")))

        assertFalse(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/deeplink/settings/language")))
        assertFalse(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/settings")))
        assertFalse(isDeepLinkSupported(Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/settings/language/")))
        assertFalse(isDeepLinkSupported(Uri.parse("https://$HOST_GODTOOLS_CUSTOM_URI/settings/language")))
    }
    // endregion isDeepLinkSupported()

    // region parseDeepLink()
    @Test
    fun `parseDeepLink() - returns LanguageSettingsScreen`() {
        assertEquals(
            listOf(LanguageSettingsScreen),
            parseDeepLink(Uri.parse("https://godtoolsapp.com/deeplink/settings/language"))
        )
    }
    // endregion parseDeepLink()
}
