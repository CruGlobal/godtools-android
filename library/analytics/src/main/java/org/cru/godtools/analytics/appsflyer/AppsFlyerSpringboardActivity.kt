package org.cru.godtools.analytics.appsflyer

import android.app.Activity
import android.os.Bundle
import org.cru.godtools.base.ui.startDashboardActivity

class AppsFlyerSpringboardActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startDashboardActivity()
        finish()
    }
}
