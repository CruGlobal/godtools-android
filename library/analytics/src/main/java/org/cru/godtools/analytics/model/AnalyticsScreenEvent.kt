package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN = "screenview"

/* Adobe Site Sections */
const val SITE_SECTION_TOOLS = "tools"
private const val SITE_SECTION_MENU = "menu"
private const val SITE_SUB_SECTION_LANGUAGE_SETTINGS = "language settings"

@Immutable
open class AnalyticsScreenEvent @JvmOverloads constructor(val screen: String, locale: Locale? = null) :
    AnalyticsBaseEvent(locale) {
    companion object {
        /* Screen event names */
        const val SCREEN_HOME = "Home"
        const val SCREEN_FIND_TOOLS = "Find Tools"
        const val SCREEN_LANGUAGE_SETTINGS = "Language Settings"
        const val SCREEN_LANGUAGE_SELECTION = "Select Language"
        const val SCREEN_MENU = "Menu"
        const val SCREEN_ABOUT = "About"
        const val SCREEN_HELP = "Help"
        const val SCREEN_CONTACT_US = "Contact Us"
        const val SCREEN_SHARE_GODTOOLS = "Share App"
        const val SCREEN_SHARE_STORY = "Share Story"
        const val SCREEN_TERMS_OF_USE = "Terms of Use"
        const val SCREEN_PRIVACY_POLICY = "Privacy Policy"
        const val SCREEN_COPYRIGHT = "Copyright Info"
    }

    override val adobeSiteSection
        get() = when (screen) {
            SCREEN_FIND_TOOLS -> SITE_SECTION_TOOLS
            SCREEN_LANGUAGE_SETTINGS,
            SCREEN_LANGUAGE_SELECTION,
            SCREEN_ABOUT, SCREEN_HELP,
            SCREEN_CONTACT_US,
            SCREEN_SHARE_GODTOOLS,
            SCREEN_SHARE_STORY,
            SCREEN_TERMS_OF_USE,
            SCREEN_PRIVACY_POLICY,
            SCREEN_COPYRIGHT -> SITE_SECTION_MENU
            else -> null
        }
    override val adobeSiteSubSection
        get() = when (screen) {
            SCREEN_LANGUAGE_SELECTION -> SITE_SUB_SECTION_LANGUAGE_SETTINGS
            else -> null
        }

    override val snowplowPageTitle get() = screen
    override val snowplowContentScoringUri: Uri.Builder
        get() = super.snowplowContentScoringUri
            .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN)
            .appendPath(screen)
}
