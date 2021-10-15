package org.cru.godtools.tract.activity

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
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
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.tool.model.Manifest
import org.keynote.godtools.android.db.GodToolsDao

private const val STATE_LIVE_SHARE_TUTORIAL_SHOWN = "liveShareTutorialShown"

@HiltViewModel
class TractActivityDataModel @Inject constructor(
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    @Named(IS_CONNECTED_LIVE_DATA) private val isConnected: LiveData<Boolean>,
    savedState: SavedStateHandle
) : BaseMultiLanguageToolActivityDataModel(dao, manifestManager, savedState) {
    val isInitialSyncFinished = MutableLiveData(false)

    // region Active Tool
    val activeLoadingState = distinctToolCode.switchCombineWith(activeLocale) { tool, l ->
        val translation = translationCache.get(tool, l)
        manifestCache.get(tool, l).combineWith(translation, isConnected, isInitialSyncFinished) { m, t, c, s ->
            LoadingState.determineToolState(m, t, Manifest.Type.TRACT, isConnected = c, isSyncFinished = s)
        }
    }.distinctUntilChanged()

    val downloadProgress = distinctToolCode.switchCombineWith(activeLocale) { t, l ->
        when {
            t == null || l == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(t, l)
        }
    }
    // endregion Active Tool

    // region Language Switcher
    val loadingState = locales
        .combineWith(manifests, translations, isConnected, isInitialSyncFinished) { l, m, t, connected, syncFinished ->
            l.associateWith {
                LoadingState.determineToolState(
                    m[it], t[it],
                    manifestType = Manifest.Type.TRACT,
                    isConnected = connected,
                    isSyncFinished = syncFinished
                )
            }
        }
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

private fun <T> LruCache<TranslationKey, T>.get(tool: String?, locale: Locale?) = get(TranslationKey(tool, locale))!!
