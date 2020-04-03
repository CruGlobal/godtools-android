package org.cru.godtools.ui.tooldetails

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

class ToolDetailsFragmentDataModel @Inject constructor(
    application: Application,
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    settings: Settings
) : ViewModel() {
    private val shortcutManager = GodToolsShortcutManager.getInstance(application)

    val toolCode = MutableLiveData<String>()
    private val distinctToolCode: LiveData<String> = toolCode.distinctUntilChanged()

    val tool = distinctToolCode.switchMap { dao.findLiveData<Tool>(it) }
    val banner = tool.map { it?.detailsBannerId }.distinctUntilChanged()
        .switchMap { it?.let { dao.findLiveData<Attachment>(it) }.orEmpty() }
    val primaryTranslation = distinctToolCode.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        dao.getLatestTranslationLiveData(tool, locale)
    }
    val parallelTranslation = distinctToolCode.switchCombineWith(settings.parallelLanguageLiveData) { tool, locale ->
        dao.getLatestTranslationLiveData(tool, locale)
    }

    val shortcut = tool.map {
        when {
            shortcutManager.canPinToolShortcut(it) -> shortcutManager.getPendingToolShortcut(it?.code)
            else -> null
        }
    }

    val downloadProgress = distinctToolCode.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        when {
            tool != null && locale != null -> downloadManager.getDownloadProgressLiveData(tool, locale)
            else -> emptyLiveData()
        }
    }

    val availableLanguages = distinctToolCode
        .switchMap { Query.select<Translation>().where(TranslationTable.FIELD_TOOL.eq(it)).getAsLiveData(dao) }
        .map { it.map { translation -> translation.languageCode }.distinct() }
}
