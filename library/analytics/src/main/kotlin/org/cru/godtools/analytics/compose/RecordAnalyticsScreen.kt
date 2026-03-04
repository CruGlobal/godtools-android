package org.cru.godtools.analytics.compose

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.compose.LocalEventBus

@Composable
fun RecordAnalyticsScreen(event: AnalyticsScreenEvent) {
    val eventBus = LocalEventBus.current
    LifecycleResumeEffect(event) {
        eventBus.post(event)
        onPauseOrDispose {}
    }
}
