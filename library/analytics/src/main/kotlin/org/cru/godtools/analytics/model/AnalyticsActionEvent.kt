package org.cru.godtools.analytics.model

import android.os.Bundle
import java.util.Locale
import javax.annotation.concurrent.Immutable

@Immutable
open class AnalyticsActionEvent(
    val action: String,
    val label: String? = null,
    locale: Locale? = null,
    systems: Collection<AnalyticsSystem> = DEFAULT_SYSTEMS
) : AnalyticsBaseEvent(locale, systems) {
    constructor(action: String, label: String? = null, locale: Locale? = null, system: AnalyticsSystem) :
        this(action, label, locale, setOf(system))

    open val firebaseEventName get() = action
    open val firebaseParams get() = Bundle()
}
