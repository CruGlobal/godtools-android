package org.cru.godtools.analytics.model

import java.util.Locale
import javax.annotation.concurrent.Immutable

@Immutable
abstract class AnalyticsBaseEvent internal constructor(
    val locale: Locale? = null,
    private val systems: Collection<AnalyticsSystem> = DEFAULT_SYSTEMS
) {
    protected companion object {
        val DEFAULT_SYSTEMS = setOf(*AnalyticsSystem.values()) - setOf(AnalyticsSystem.APPSFLYER, AnalyticsSystem.USER)
    }

    /**
     * Return whether or not this Analytics event should be tracked in the specified service
     */
    open fun isForSystem(system: AnalyticsSystem) = systems.contains(system)

    open val appSection: String? get() = null
    open val appSubSection: String? get() = null

    open val userCounterName: String? get() = null

    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        other !is AnalyticsBaseEvent -> false
        locale != other.locale -> false
        else -> true
    }

    override fun hashCode() = locale?.hashCode() ?: 0
}
