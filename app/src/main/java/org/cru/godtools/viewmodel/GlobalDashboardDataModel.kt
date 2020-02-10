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

    val uniqueUsers = globalActivityAnalytics.map { it?.users.formatNumber() }
    val gospelPresentation = globalActivityAnalytics.map { it?.gospelPresentation.formatNumber() }
    val sessions = globalActivityAnalytics.map { it?.launches.formatNumber() }
    val countries = globalActivityAnalytics.map { it?.countries.formatNumber() }

    private fun Int?.formatNumber(): String {
        return NumberFormat.getInstance().format(this ?: 0)
    }
}
