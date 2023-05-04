package org.cru.godtools.analytics.model

import java.util.Locale
import javax.annotation.concurrent.Immutable
import org.cru.godtools.shared.analytics.AnalyticsAppSectionNames

@Immutable
open class AnalyticsScreenEvent(val screen: String, locale: Locale? = null) : AnalyticsBaseEvent(locale) {
    override val appSection get() = AnalyticsAppSectionNames.forScreen(screen) ?: super.appSection
    override val appSubSection get() = AnalyticsAppSectionNames.subSectionForScreen(screen) ?: super.appSubSection

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        !super.equals(other) -> false
        other !is AnalyticsScreenEvent -> false
        screen != other.screen -> false
        else -> true
    }

    override fun hashCode() = (super.hashCode() * 31) + screen.hashCode()
}
