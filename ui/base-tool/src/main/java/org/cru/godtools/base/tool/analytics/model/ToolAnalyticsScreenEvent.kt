package org.cru.godtools.base.tool.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.tool.model.Manifest

const val SCREEN_CATEGORIES = "Categories"

open class ToolAnalyticsScreenEvent(
    screen: String,
    private val tool: String?,
    locale: Locale? = null
) : AnalyticsScreenEvent(screen, locale) {
    protected constructor(screen: String, manifest: Manifest) : this(screen, manifest.code, manifest.locale)

    override val appSection get() = tool
}
