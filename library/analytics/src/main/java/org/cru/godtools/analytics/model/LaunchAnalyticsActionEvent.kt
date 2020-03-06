package org.cru.godtools.analytics.model

private const val ACTION_LAUNCH = "App Launch"
private const val FIREBASE_EVENT_LAUNCH_PREFIX = "launch_"

class LaunchAnalyticsActionEvent(private val launches: Int) : AnalyticsActionEvent(action = ACTION_LAUNCH) {
    override fun isForSystem(system: AnalyticsSystem) = when (launches) {
        1, 2, 3, 5, 10 -> system == AnalyticsSystem.FIREBASE
        else -> false
    }

    override val firebaseEventName get() = "$FIREBASE_EVENT_LAUNCH_PREFIX$launches"
}
