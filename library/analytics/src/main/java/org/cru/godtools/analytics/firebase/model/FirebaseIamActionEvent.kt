package org.cru.godtools.analytics.firebase.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

const val ACTION_IAM_MY_TOOLS = "iam_mytools"

class FirebaseIamActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}
