package org.cru.godtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.GodToolsDao
import java.text.NumberFormat

class GlobalDashboardDataModel(app: Application) : AndroidViewModel(app) {

    private val globalActivityAnalytics =
        GodToolsDao.getInstance(getApplication()).findLiveData(GlobalActivityAnalytics::class.java, 1)

    val uniqueUsers = globalActivityAnalytics.map {
        NumberFormat.getInstance().format(it?.users ?: 0)
    }
    val gospelPresentation = globalActivityAnalytics.map {
        NumberFormat.getInstance().format(it?.gospelPresentation ?: 0)
    }
    val sessions = globalActivityAnalytics.map {
        NumberFormat.getInstance().format(it?.launches ?: 0)
    }
    val countries = globalActivityAnalytics.map {
        NumberFormat.getInstance().format(it?.countries ?: 0)
    }
}
