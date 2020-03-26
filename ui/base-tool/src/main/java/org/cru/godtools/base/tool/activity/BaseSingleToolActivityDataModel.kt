package org.cru.godtools.base.tool.activity

import android.app.Application
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.keynote.godtools.android.db.GodToolsDao

internal class BaseSingleToolActivityDataModel(app: Application) : LatestPublishedManifestDataModel(app) {
    private val dao = GodToolsDao.getInstance(app)

    val translation = toolCode.switchCombineWith(locale) { code, locale ->
        when {
            code == null || locale == null -> emptyLiveData()
            else -> dao.getLatestTranslationLiveData(code, locale, trackAccess = true)
        }
    }
}
