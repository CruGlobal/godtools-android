package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.lifecycle.orEmpty
import org.ccci.gto.android.common.lifecycle.switchCombineWith
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

class ToolDetailsFragmentDataModel(application: Application) : AndroidViewModel(application) {
    private val dao = GodToolsDao.getInstance(application)
    private val settings = Settings.getInstance(application)

    val toolCode = MutableLiveData<String>()
    private val distinctToolCode = toolCode.distinctUntilChanged()

    val tool = distinctToolCode.switchMap { it?.let { dao.findLiveData<Tool>(it) }.orEmpty() }
    val banner = tool.map { it?.detailsBannerId }.distinctUntilChanged()
        .switchMap { it?.let { dao.findLiveData<Attachment>(it) }.orEmpty() }
    val primaryTranslation = distinctToolCode.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        dao.getLatestTranslationLiveData(tool, locale)
    }
    val parallelTranslation = distinctToolCode.switchCombineWith(settings.parallelLanguageLiveData) { tool, locale ->
        dao.getLatestTranslationLiveData(tool, locale)
    }
}
