package org.cru.godtools.ui.tooldetails

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.findLiveData
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.shortcuts.GodToolsShortcutManager
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

private const val KEY_TIPS_LANGUAGE = "KEY_TIPS_LANGUAGE"
private const val KEY_TIPS_TOOL = "KEY_TIPS_TOOL"
private const val KEY_TIPS_TYPE = "KEY_TIPS_TYPE"

class ToolDetailsFragmentDataModel @ViewModelInject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    settings: Settings,
    private val shortcutManager: GodToolsShortcutManager,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
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

    val primaryManifest = toolCode.switchCombineWith(settings.primaryLanguageLiveData) { code, locale ->
        when (code) {
            null -> emptyLiveData()
            else -> manifestManager.getLatestPublishedManifestLiveData(code, locale)
        }
    }

    val shortcut = tool.map {
        when {
            shortcutManager.canPinToolShortcut(it) -> shortcutManager.getPendingToolShortcut(it?.code)
            else -> null
        }
    }

    val downloadProgress = distinctToolCode.switchCombineWith(settings.primaryLanguageLiveData) { tool, locale ->
        downloadManager.getDownloadProgressLiveData(tool, locale)
    }

    val availableLanguages = distinctToolCode
        .switchMap { Query.select<Translation>().where(TranslationTable.FIELD_TOOL.eq(it)).getAsLiveData(dao) }
        .map { it.map { translation -> translation.languageCode }.distinct() }

    // region tipsSavedState
    var tipsLanguage: Locale?
        get() = savedStateHandle.get<Locale>(KEY_TIPS_LANGUAGE)
        set(value) = savedStateHandle.set(KEY_TIPS_LANGUAGE, value)

    var tipsTool: String?
        get() = savedStateHandle.get<String>(KEY_TIPS_TOOL)
        set(value) = savedStateHandle.set(KEY_TIPS_TOOL, value)

    var tipsType: Tool.Type?
        get() = savedStateHandle.get<Tool.Type>(KEY_TIPS_TYPE)
        set(value) = savedStateHandle.set(KEY_TIPS_TYPE, value)
    // endregion tipsSavedState
}
