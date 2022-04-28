package org.cru.godtools.analytics.firebase.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

const val ACTION_IAM_HOME = "iam_mytools"
const val ACTION_IAM_LESSONS = "iam_lessons"
const val ACTION_IAM_ALL_TOOLS = "iam_tools"

class FirebaseIamActionEvent(action: String) : AnalyticsActionEvent(action) {
    override fun isForSystem(system: AnalyticsSystem) = system == AnalyticsSystem.FIREBASE
}
