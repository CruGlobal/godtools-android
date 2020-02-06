package org.cru.godtools.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import org.ccci.gto.android.common.db.Query
import org.cru.godtools.model.GlobalActivityAnalytics
import org.keynote.godtools.android.db.GodToolsDao

class GlobalDashboardDataModel(app: Application) : AndroidViewModel(app) {

    private val dao = GodToolsDao.getInstance(getApplication())
        .getLiveData(Query.select(GlobalActivityAnalytics::class.java))

    val uniqueUsers = dao.map { "${it.first().users}" }
    val gospelPresentation = dao.map { "${it.first().gospelPresentation}" }
    val sessions = dao.map { "${it.first().launches}" }
    val countries = dao.map { "${it.first().countries}" }
}
