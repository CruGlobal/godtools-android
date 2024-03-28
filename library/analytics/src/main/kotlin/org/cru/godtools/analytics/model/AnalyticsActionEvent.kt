package org.cru.godtools.analytics.model

import android.os.Bundle
import java.util.Locale
import javax.annotation.concurrent.Immutable

@Immutable
open class AnalyticsActionEvent(
    val action: String,
    val label: String? = null,
    locale: Locale? = null,
    systems: Set<AnalyticsSystem> = DEFAULT_SYSTEMS
) : AnalyticsBaseEvent(locale, systems) {
    constructor(action: String, label: String? = null, locale: Locale? = null, system: AnalyticsSystem) :
        this(action, label, locale, setOf(system))

    open val firebaseEventName get() = action
    open val firebaseParams get() = Bundle()

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        !super.equals(other) -> false
        other !is AnalyticsActionEvent -> false
        action != other.action -> false
        label != other.label -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + action.hashCode()
        result = 31 * result + (label?.hashCode() ?: 0)
        return result
    }
}
