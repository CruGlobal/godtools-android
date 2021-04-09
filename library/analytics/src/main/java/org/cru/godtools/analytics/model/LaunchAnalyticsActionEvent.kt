package org.cru.godtools.analytics.model

private const val ACTION_LAUNCH_PREFIX = "launch_"
private const val ACTION_LAUNCH_AT_LEAST_PREFIX = "launch_atleast_"

class LaunchAnalyticsActionEvent(private val launches: Int) : AnalyticsActionEvent(action = launches.buildAction()) {
    override fun isForSystem(system: AnalyticsSystem) = launches > 0 && system == AnalyticsSystem.FIREBASE
}

private fun Int.buildAction() = when (this) {
    1, 2 -> "$ACTION_LAUNCH_PREFIX$this"
    in 3..4 -> "${ACTION_LAUNCH_AT_LEAST_PREFIX}3"
    in 5..9 -> "${ACTION_LAUNCH_AT_LEAST_PREFIX}5"
    in 10..Int.MAX_VALUE -> "${ACTION_LAUNCH_AT_LEAST_PREFIX}10"
    else -> "invalid"
}
