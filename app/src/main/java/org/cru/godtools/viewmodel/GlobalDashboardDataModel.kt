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

    private val dao : LiveData<List<GlobalActivityAnalytics>>? by lazy {  Query.select(GlobalActivityAnalytics::class.java)
        .getAsLiveData(GodToolsDao.getInstance(getApplication())) }

    private val globalActivityAnalytics by lazy{ dao?.map { it.first() } }

    val uniqueUsers = globalActivityAnalytics?.map { "${it.users}" }
    val gospelPresentation = globalActivityAnalytics?.map { "${it.gospelPresentation}" }
    val sessions = globalActivityAnalytics?.map { "${it.launches}" }
    val countries = globalActivityAnalytics?.map { "${it.countries}" }
}
