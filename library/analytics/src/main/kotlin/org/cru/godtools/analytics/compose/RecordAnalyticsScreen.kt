package org.cru.godtools.analytics.compose

import androidx.compose.runtime.Composable
import org.ccci.gto.android.common.androidx.lifecycle.compose.OnResume
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.ui.compose.LocalEventBus

@Composable
fun RecordAnalyticsScreen(event: AnalyticsScreenEvent) {
    val eventBus = LocalEventBus.current
    OnResume(event) { eventBus.post(event) }
}
