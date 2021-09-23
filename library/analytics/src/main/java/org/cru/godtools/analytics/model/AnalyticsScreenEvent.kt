package org.cru.godtools.analytics.model

import android.net.Uri
import java.util.Locale
import javax.annotation.concurrent.Immutable

private const val SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN = "screenview"

/* App Sections */
private const val APP_SECTION_MENU = "menu"
private const val APP_SUB_SECTION_LANGUAGE_SETTINGS = "language settings"

@Immutable
open class AnalyticsScreenEvent(val screen: String, locale: Locale? = null) : AnalyticsBaseEvent(locale) {
    companion object {
        /* Screen event names */
        const val SCREEN_HOME = "Favorites"
        const val SCREEN_ALL_TOOLS = "All Tools"
        const val SCREEN_LESSONS = "Lessons"
        const val SCREEN_LANGUAGE_SETTINGS = "Language Settings"
        const val SCREEN_LANGUAGE_SELECTION = "Select Language"
        const val SCREEN_ABOUT = "About"
        const val SCREEN_HELP = "Help"
        const val SCREEN_CONTACT_US = "Contact Us"
        const val SCREEN_SHARE_GODTOOLS = "Share App"
        const val SCREEN_SHARE_STORY = "Share Story"
        const val SCREEN_TERMS_OF_USE = "Terms of Use"
        const val SCREEN_PRIVACY_POLICY = "Privacy Policy"
        const val SCREEN_COPYRIGHT = "Copyright Info"
        const val SCREEN_GLOBAL_DASHBOARD = "Global Dashboard"

        /* App Sections */
        const val APP_SECTION_TOOLS = "tools"
    }

    override fun isForSystem(system: AnalyticsSystem) = when (screen) {
        SCREEN_LESSONS -> system == AnalyticsSystem.APPSFLYER || super.isForSystem(system)
        else -> super.isForSystem(system)
    }

    override val appSection
        get() = when (screen) {
            SCREEN_ALL_TOOLS -> APP_SECTION_TOOLS
            SCREEN_LANGUAGE_SETTINGS,
            SCREEN_LANGUAGE_SELECTION,
            SCREEN_ABOUT, SCREEN_HELP,
            SCREEN_CONTACT_US,
            SCREEN_SHARE_GODTOOLS,
            SCREEN_SHARE_STORY,
            SCREEN_TERMS_OF_USE,
            SCREEN_PRIVACY_POLICY,
            SCREEN_COPYRIGHT -> APP_SECTION_MENU
            else -> super.appSection
        }
    override val appSubSection
        get() = when (screen) {
            SCREEN_LANGUAGE_SELECTION -> APP_SUB_SECTION_LANGUAGE_SETTINGS
            else -> super.appSubSection
        }

    override val snowplowPageTitle get() = screen
    override val snowplowContentScoringUri: Uri.Builder
        get() = super.snowplowContentScoringUri
            .authority(SNOWPLOW_CONTENT_SCORING_URI_PATH_SCREEN)
            .appendPath(screen)
}
