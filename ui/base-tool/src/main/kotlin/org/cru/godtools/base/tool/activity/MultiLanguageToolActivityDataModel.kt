package org.cru.godtools.base.tool.activity

import androidx.annotation.VisibleForTesting
import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.and
import org.ccci.gto.android.common.androidx.lifecycle.combine
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.livedata
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.androidx.lifecycle.switchFold
import org.ccci.gto.android.common.androidx.lifecycle.withInitialValue
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.activity.BaseToolActivity.LoadingState
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.EXTRA_SHOW_TIPS
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.user.activity.UserActivityManager

private const val STATE_PRIMARY_LOCALES = "primaryLocales"
private const val STATE_PARALLEL_LOCALES = "parallelLocales"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MultiLanguageToolActivityDataModel @Inject constructor(
    downloadManager: GodToolsDownloadManager,
    languagesRepository: LanguagesRepository,
    manifestManager: ManifestManager,
    toolsRepository: ToolsRepository,
    translationsRepository: TranslationsRepository,
    userActivityManager: UserActivityManager,
    @Named(IS_CONNECTED_LIVE_DATA) isConnected: LiveData<Boolean>,
    savedState: SavedStateHandle,
) : BaseToolRendererViewModel(downloadManager, manifestManager, userActivityManager, savedState) {
    val primaryLocales = savedState.getLiveData<List<Locale>>(STATE_PRIMARY_LOCALES, emptyList())
    val parallelLocales = savedState.getLiveData<List<Locale>>(STATE_PARALLEL_LOCALES, emptyList())

    // region LiveData Caches
    private val manifestCache = object : LruCache<TranslationKey, Flow<Manifest?>>(10) {
        override fun create(key: TranslationKey): Flow<Manifest?> {
            val tool = key.tool ?: return flowOf(null)
            val locale = key.locale ?: return flowOf(null)
            return manifestManager.getLatestPublishedManifestFlow(tool, locale)
                .distinctUntilChanged()
                .shareIn(viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000), replay = 1)
        }
    }

    private val translationCache = object : LruCache<TranslationKey, LiveData<Translation?>>(10) {
        override fun create(key: TranslationKey) =
            translationsRepository.findLatestTranslationFlow(key.tool, key.locale)
                .distinctUntilChanged()
                .asLiveData()
    }
    // endregion LiveData Caches

    // region Resolved Data
    private val distinctToolCode = savedState.getLiveData<String?>(EXTRA_TOOL).distinctUntilChanged()
    val locales = combine(
        savedState.getStateFlow<List<Locale>>(STATE_PRIMARY_LOCALES, emptyList()),
        savedState.getStateFlow<List<Locale>>(STATE_PARALLEL_LOCALES, emptyList())
    ) { prim, para -> (prim + para).distinct() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tool = toolCode.flatMapLatest { it?.let { toolsRepository.findToolFlow(it) } ?: flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val languages = locales
        .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
        .map { it.associateBy { it.code } }
        .asLiveData()

    @VisibleForTesting
    internal val translations =
        locales.asLiveData().switchFold(ImmutableLiveData(emptyList<Pair<Locale, Translation?>>())) { acc, locale ->
            distinctToolCode.switchMap { translationCache.get(it, locale).withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, translations -> translations + Pair(locale, it) }
        }.map { it.toMap() }

    @VisibleForTesting
    internal val manifests =
        locales.asLiveData().switchFold(ImmutableLiveData(emptyList<Pair<Locale, Manifest?>>())) { acc, locale ->
            distinctToolCode.switchMap { manifestCache.get(it, locale).asLiveData().withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, manifests -> manifests + Pair(locale, it) }
        }.map { it.toMap() }
    // endregion Resolved Data

    // region Loading State
    val isInitialSyncFinished = MutableStateFlow(false)

    val loadingState = locales.asLiveData().combineWith(
        manifests,
        translations,
        supportedType.asLiveData(),
        isConnected,
        isInitialSyncFinished.asLiveData()
    ) { l, m, t, type, connected, syncFinished ->
        l.associateWith {
            LoadingState.determineToolState(
                m[it],
                t[it],
                manifestType = type,
                isConnected = connected,
                isSyncFinished = syncFinished
            )
        }
    }.distinctUntilChanged()
    // endregion Loading State

    // region Active Tool
    val activeLocale by savedState.livedata<Locale?>(STATE_ACTIVE_LOCALE)

    val activeLoadingState = distinctToolCode.switchCombineWith(activeLocale) { tool, l ->
        combine(
            manifestCache.get(tool, l).asLiveData(),
            translationCache.get(tool, l),
            supportedType.asLiveData(),
            isConnected,
            isInitialSyncFinished.asLiveData()
        ) { m, t, type, c, s ->
            LoadingState.determineToolState(m, t, type, isConnected = c, isSyncFinished = s)
        }
    }.distinctUntilChanged()

    val activeManifest = manifest.asLiveData()
    // endregion Active Tool

    // region Available Locales
    @VisibleForTesting
    internal val availableLocales =
        combine(activeLocale, primaryLocales, parallelLocales, loadingState) { active, primary, parallel, loaded ->
            buildList {
                primary
                    .filterNot { loaded[it] == LoadingState.INVALID_TYPE || loaded[it] == LoadingState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == active && loaded[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loaded[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == active }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
                parallel
                    .filterNot { contains(it) }
                    .filterNot { loaded[it] == LoadingState.INVALID_TYPE || loaded[it] == LoadingState.NOT_FOUND }
                    .let {
                        it.firstOrNull { it == active && loaded[it] != LoadingState.OFFLINE }
                            ?: it.firstOrNull { loaded[it] == LoadingState.LOADED }
                            ?: it.firstOrNull { it == active }
                            ?: it.firstOrNull()
                    }
                    ?.let { add(it) }
            }
        }.distinctUntilChanged()
    val visibleLocales = availableLocales.combineWith(loadingState) { locales, loadingState ->
        locales.filter { loadingState[it] == LoadingState.LOADED }
    }
    // endregion Available Locales

    // region Training Tips
    val showTips: MutableLiveData<Boolean> = savedState.getLiveData(EXTRA_SHOW_TIPS, false)

    val hasTips = activeManifest.map { it?.hasTips == true }
    val enableTips = hasTips and showTips
    // endregion Training Tips

    init {
        // initialize the activeLocale if it hasn't been initialized yet
        locales.asLiveData().map { it.firstOrNull() }.notNull().observeOnce(this) {
            if (activeLocale.value == null) activeLocale.value = it
        }

        // update the activeLocale if the current activeLocale is invalid
        observe(
            activeLoadingState,
            availableLocales,
            loadingState
        ) { activeLoadingState, availableLocales, loadingState ->
            when (activeLoadingState) {
                // update the active language if the current active language is not found, invalid, or offline
                LoadingState.NOT_FOUND,
                LoadingState.INVALID_TYPE,
                LoadingState.OFFLINE -> availableLocales.firstOrNull {
                    loadingState[it] != LoadingState.NOT_FOUND && loadingState[it] != LoadingState.INVALID_TYPE &&
                        loadingState[it] != LoadingState.OFFLINE
                }?.let { activeLocale.value = it }
                else -> Unit
            }
        }
    }
}

private fun <T> LruCache<TranslationKey, T>.get(tool: String?, locale: Locale?) = get(TranslationKey(tool, locale))!!
