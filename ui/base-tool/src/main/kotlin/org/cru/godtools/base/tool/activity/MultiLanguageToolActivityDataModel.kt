package org.cru.godtools.base.tool.activity

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
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.tool.model.Manifest
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao

private const val STATE_ACTIVE_LOCALE = "activeLocale"

@HiltViewModel
open class MultiLanguageToolActivityDataModel @Inject constructor(
    dao: GodToolsDao,
    downloadManager: GodToolsDownloadManager,
    manifestManager: ManifestManager,
    @Named(IS_CONNECTED_LIVE_DATA) isConnected: LiveData<Boolean>,
    protected val savedState: SavedStateHandle,
) : ViewModel() {
    val toolCode = MutableLiveData<String?>()
    val primaryLocales = MutableLiveData<List<Locale>>(emptyList())
    val parallelLocales = MutableLiveData<List<Locale>>(emptyList())

    // region Resolved Data
    val locales = primaryLocales.combineWith(parallelLocales) { primary, parallel -> primary + parallel }
    private val distinctToolCode = toolCode.distinctUntilChanged()
    private val distinctLocales = locales.distinctUntilChanged()

    val languages = distinctLocales.switchMap {
        Query.select<Language>()
            .where(LanguageTable.FIELD_CODE.`in`(*Expression.constants(*it.toTypedArray())))
            .getAsLiveData(dao)
    }.map { it.associateBy { it.code } }

    @VisibleForTesting
    internal val translations =
        distinctLocales.switchFold(ImmutableLiveData(emptyList<Pair<Locale, Translation?>>())) { acc, locale ->
            distinctToolCode.switchMap { translationCache.get(it, locale).withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, translations -> translations + Pair(locale, it) }
        }.map { it.toMap() }

    @VisibleForTesting
    internal val manifests =
        distinctLocales.switchFold(ImmutableLiveData(emptyList<Pair<Locale, Manifest?>>())) { acc, locale ->
            distinctToolCode.switchMap { manifestCache.get(it, locale).withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, manifests -> manifests + Pair(locale, it) }
        }.map { it.toMap() }
    // endregion Resolved Data

    // region Loading State
    val isInitialSyncFinished = MutableLiveData(false)
    val supportedType = MutableLiveData<Manifest.Type>()

    val loadingState = locales.combineWith(
        manifests,
        translations,
        supportedType,
        isConnected,
        isInitialSyncFinished
    ) { l, m, t, type, connected, syncFinished ->
        l.associateWith {
            LoadingState.determineToolState(
                m[it], t[it],
                manifestType = type,
                isConnected = connected,
                isSyncFinished = syncFinished
            )
        }
    }
    // endregion Loading State

    // region Active Tool
    val activeLocale = savedState.getLiveData<String?>(STATE_ACTIVE_LOCALE)
        .map { it?.let { Locale.forLanguageTag(it) } }
        .distinctUntilChanged()
    fun setActiveLocale(locale: Locale) = savedState.set(STATE_ACTIVE_LOCALE, locale.toLanguageTag())

    val activeLoadingState = distinctToolCode.switchCombineWith(activeLocale) { tool, l ->
        val manifest = manifestCache.get(tool, l)
        val translation = translationCache.get(tool, l)
        manifest.combineWith(translation, supportedType, isConnected, isInitialSyncFinished) { m, t, type, c, s ->
            LoadingState.determineToolState(m, t, type, isConnected = c, isSyncFinished = s)
        }
    }.distinctUntilChanged()

    val activeManifest =
        distinctToolCode.switchCombineWith(activeLocale) { t, l -> manifestCache.get(t, l).withInitialValue(null) }
            .combineWith(supportedType) { manifest, type -> manifest?.takeIf { it.type == type } }

    internal val activeToolDownloadProgress = distinctToolCode.switchCombineWith(activeLocale) { t, l ->
        when {
            t == null || l == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(t, l)
        }
    }
    // endregion Active Tool

    // region Available Locales
    @OptIn(ExperimentalStdlibApi::class)
    val availableLocales = activeLocale
        .combineWith(primaryLocales, parallelLocales, loadingState) { activeLocale, primary, parallel, loaded ->
            buildList {
                primary
                    .filterNot { loaded[it] == LoadingState.INVALID_TYPE || loaded[it] == LoadingState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == activeLocale && loaded[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loaded[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
                parallel
                    .filterNot { contains(it) }
                    .filterNot { loaded[it] == LoadingState.INVALID_TYPE || loaded[it] == LoadingState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == activeLocale && loaded[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loaded[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == activeLocale }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
            }
        }
    val visibleLocales = availableLocales.combineWith(loadingState) { locales, loadingState ->
        locales.filter { loadingState[it] == LoadingState.LOADED }
    }
    // endregion Available Locales

    private val translationCache = object : LruCache<TranslationKey, LiveData<Translation?>>(10) {
        override fun create(key: TranslationKey) =
            dao.getLatestTranslationLiveData(key.tool, key.locale, trackAccess = true).distinctUntilChanged()
    }

    private val manifestCache = object : LruCache<TranslationKey, LiveData<Manifest?>>(10) {
        override fun create(key: TranslationKey): LiveData<Manifest?> {
            val tool = key.tool ?: return emptyLiveData()
            val locale = key.locale ?: return emptyLiveData()
            return manifestManager.getLatestPublishedManifestLiveData(tool, locale).distinctUntilChanged()
        }
    }
}

private fun <T> LruCache<TranslationKey, T>.get(tool: String?, locale: Locale?) = get(TranslationKey(tool, locale))!!
