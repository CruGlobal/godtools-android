package org.cru.godtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.GodToolsDao

class GlobalDashboardDataModel(app: Application) : AndroidViewModel(app) {

    private val dao: LiveData<List<GlobalActivityAnalytics>>? by lazy {
        Query.select(GlobalActivityAnalytics::class.java)
            .getAsLiveData(GodToolsDao.getInstance(getApplication()))
    }

    private val globalActivityAnalytics by lazy {
        dao?.map {
            if (it.count() > 0) {
                return@map it.first()
            }
            return@map null
        }
    }

    val uniqueUsers = globalActivityAnalytics?.map { "${it?.users ?: 0}" }
    val gospelPresentation = globalActivityAnalytics?.map { "${it?.gospelPresentation ?: 0}" }
    val sessions = globalActivityAnalytics?.map { "${it?.launches ?: 0}" }
    val countries = globalActivityAnalytics?.map { "${it?.countries ?: 0}" }
}
