package org.cru.godtools.tract.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.activity.BaseMultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.keynote.godtools.android.db.GodToolsDao

private const val STATE_LIVE_SHARE_TUTORIAL_SHOWN = "liveShareTutorialShown"

@HiltViewModel
class TractActivityDataModel @Inject constructor(
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    @Named(IS_CONNECTED_LIVE_DATA) isConnected: LiveData<Boolean>,
    savedState: SavedStateHandle
) : BaseMultiLanguageToolActivityDataModel(dao, manifestManager, isConnected, savedState) {
    // region Active Tool
    val downloadProgress = distinctToolCode.switchCombineWith(activeLocale) { t, l ->
        when {
            t == null || l == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(t, l)
        }
    }
    // endregion Active Tool

    // region Language Switcher
    @OptIn(ExperimentalStdlibApi::class)
    val availableLocales = activeLocale
        .combineWith(primaryLocales, parallelLocales, loadingState) { activeLocale, primary, parallel, loadingState ->
            buildList {
                primary
                    .filterNot {
                        loadingState[it] == LoadingState.INVALID_TYPE || loadingState[it] == LoadingState.NOT_FOUND
                    }
                    .let {
                        it.firstOrNull { it == activeLocale && loadingState[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loadingState[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
                parallel
                    .filterNot { contains(it) }
                    .filterNot {
                        loadingState[it] == LoadingState.INVALID_TYPE || loadingState[it] == LoadingState.NOT_FOUND
                    }
                    .let {
                        it.firstOrNull { it == activeLocale && loadingState[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loadingState[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
            }
        }
    val visibleLocales = availableLocales.combineWith(loadingState) { locales, loadingState ->
        locales.filter { loadingState[it] == LoadingState.LOADED }
    }
    // endregion Language Switcher

    var liveShareTutorialShown: Boolean
        get() = savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] ?: false
        set(value) {
            savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] = value
        }
}
