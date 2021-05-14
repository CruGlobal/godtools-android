package org.cru.godtools.tract.activity

import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.androidx.lifecycle.switchFold
import org.ccci.gto.android.common.androidx.lifecycle.withInitialValue
import org.ccci.gto.android.common.db.Expression
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.tool.BaseToolRendererModule.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.activity.BaseToolActivity.ToolState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.xml.model.Manifest
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao

private const val STATE_ACTIVE_LOCALE = "activeLocale"
private const val STATE_LIVE_SHARE_TUTORIAL_SHOWN = "liveShareTutorialShown"

@HiltViewModel
class TractActivityDataModel @Inject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val manifestManager: ManifestManager,
    @Named(IS_CONNECTED_LIVE_DATA) private val isConnected: LiveData<Boolean>,
    private val savedState: SavedStateHandle
) : ViewModel() {
    val tool = MutableLiveData<String?>()
    val isInitialSyncFinished = MutableLiveData(false)
    private val distinctTool = tool.distinctUntilChanged()

    // region Active Tool
    val activeLocale = savedState.getLiveData<String?>(STATE_ACTIVE_LOCALE)
        .map { it?.let { Locale.forLanguageTag(it) } }
        .distinctUntilChanged()
    fun setActiveLocale(locale: Locale) = savedState.set(STATE_ACTIVE_LOCALE, locale.toLanguageTag())

    val activeManifest =
        distinctTool.switchCombineWith(activeLocale) { t, l -> manifestCache.get(t, l).withInitialValue(null) }
            .map { it?.takeIf { it.type == Manifest.Type.TRACT } }
    val activeState = distinctTool.switchCombineWith(activeLocale) { tool, l ->
        val translation = translationCache.get(tool, l)
        manifestCache.get(tool, l).combineWith(translation, isConnected, isInitialSyncFinished) { m, t, c, s ->
            ToolState.determineToolState(m, t, manifestType = Manifest.Type.TRACT, isConnected = c, isSyncFinished = s)
        }
    }.distinctUntilChanged()

    val downloadProgress = distinctTool.switchCombineWith(activeLocale) { t, l ->
        when {
            t == null || l == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(t, l)
        }
    }
    // endregion Active Tool

    // region Language Switcher
    val primaryLocales = MutableLiveData<List<Locale>>(emptyList())
    val parallelLocales = MutableLiveData<List<Locale>>(emptyList())
    val locales = primaryLocales.combineWith(parallelLocales) { primary, parallel -> primary + parallel }
    private val distinctLocales = locales.distinctUntilChanged()

    val languages = distinctLocales.switchMap {
        Query.select<Language>()
            .where(LanguageTable.FIELD_CODE.`in`(*Expression.constants(*it.toTypedArray())))
            .getAsLiveData(dao)
    }.map { it.associateBy { it.code } }
    @VisibleForTesting
    internal val manifests =
        distinctLocales.switchFold(ImmutableLiveData(emptyList<Pair<Locale, Manifest?>>())) { acc, locale ->
            distinctTool.switchMap { manifestCache.get(it, locale).withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, manifests -> manifests + Pair(locale, it) }
        }.map { it.toMap() }
    @VisibleForTesting
    internal val translations =
        distinctLocales.switchFold(ImmutableLiveData(emptyList<Pair<Locale, Translation?>>())) { acc, locale ->
            distinctTool.switchMap { translationCache.get(it, locale).withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, translations -> translations + Pair(locale, it) }
        }.map { it.toMap() }
    val state = locales
        .combineWith(manifests, translations, isConnected, isInitialSyncFinished) { l, m, t, connected, syncFinished ->
            l.associateWith {
                ToolState.determineToolState(
                    m[it], t[it],
                    manifestType = Manifest.Type.TRACT,
                    isConnected = connected,
                    isSyncFinished = syncFinished
                )
            }
        }
    @OptIn(ExperimentalStdlibApi::class)
    val availableLocales =
        activeLocale.combineWith(primaryLocales, parallelLocales, state) { activeLocale, primary, parallel, state ->
            buildList {
                primary
                    .filterNot { state[it] == ToolState.INVALID_TYPE || state[it] == ToolState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == activeLocale && state[it] != ToolState.OFFLINE }
                            ?: it.firstOrNull { state[it] == ToolState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
                parallel
                    .filterNot { contains(it) }
                    .filterNot { state[it] == ToolState.INVALID_TYPE || state[it] == ToolState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == activeLocale && state[it] != ToolState.OFFLINE }
                            ?: it.firstOrNull { state[it] == ToolState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
            }
        }
    val visibleLocales =
        availableLocales.combineWith(state) { locales, state -> locales.filter { state[it] == ToolState.LOADED } }
    // endregion Language Switcher

    var liveShareTutorialShown: Boolean
        get() = savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] ?: false
        set(value) {
            savedState[STATE_LIVE_SHARE_TUTORIAL_SHOWN] = value
        }

    private val manifestCache = object : LruCache<TranslationKey, LiveData<Manifest?>>(10) {
        override fun create(key: TranslationKey): LiveData<Manifest?> {
            val tool = key.tool ?: return emptyLiveData()
            val locale = key.locale ?: return emptyLiveData()
            return manifestManager.getLatestPublishedManifestLiveData(tool, locale).distinctUntilChanged()
        }
    }
    private val translationCache = object : LruCache<TranslationKey, LiveData<Translation?>>(10) {
        override fun create(key: TranslationKey) =
            dao.getLatestTranslationLiveData(key.tool, key.locale, trackAccess = true).distinctUntilChanged()
    }
}

private fun <T> LruCache<TranslationKey, T>.get(tool: String?, locale: Locale?) = get(TranslationKey(tool, locale))!!
