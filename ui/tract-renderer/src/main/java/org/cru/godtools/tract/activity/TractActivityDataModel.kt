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
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchCombineWith
import org.ccci.gto.android.common.androidx.lifecycle.switchFold
import org.ccci.gto.android.common.androidx.lifecycle.withInitialValue
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.tract.activity.KotlinTractActivity.Companion.determineState
import org.cru.godtools.xml.model.Manifest
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

private const val STATE_ACTIVE_LOCALE = "activeLocale"

class TractActivityDataModel @AssistedInject constructor(
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val manifestManager: ManifestManager,
    @Assisted private val savedState: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<TractActivityDataModel>

    val tool = MutableLiveData<String?>()
    val isSyncRunning = MutableLiveData<Boolean?>(null)
    private val distinctTool = tool.distinctUntilChanged()

    // region Active Tool
    val activeLocale = savedState.getLiveData<String?>(STATE_ACTIVE_LOCALE)
        .map { it?.let { LocaleCompat.forLanguageTag(it) } }
        .distinctUntilChanged()
    fun setActiveLocale(locale: Locale?) =
        savedState.set(STATE_ACTIVE_LOCALE, locale?.let { LocaleCompat.toLanguageTag(locale) })

    private val rawActiveManifest = distinctTool.switchCombineWith(activeLocale) { t, l ->
        manifestCache.get(TranslationKey(t, l))!!.withInitialValue(null)
    }
    val activeManifest = rawActiveManifest.map { it?.takeIf { it.type == Manifest.Type.TRACT } }
    private val activeTranslation = distinctTool.switchCombineWith(activeLocale) { t, l ->
        translationCache.get(TranslationKey(t, l))!!.withInitialValue(null)
    }
    val activeState = rawActiveManifest.combineWith(activeTranslation, isSyncRunning) { m, t, s ->
        determineState(m, t, s)
    }

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

    val manifests: LiveData<List<Manifest?>> =
        distinctLocales.switchFold(ImmutableLiveData(emptyList())) { acc, locale ->
            val manifest =
                distinctTool.switchMap { manifestCache.get(TranslationKey(it, locale))!!.withInitialValue(null) }
                    .distinctUntilChanged()
            acc.distinctUntilChanged().combineWith(manifest) { manifests, manifest -> manifests + manifest }
        }
    @VisibleForTesting
    internal val translations =
        distinctLocales.switchFold(ImmutableLiveData(emptyList<Pair<Locale, Translation?>>())) { acc, locale ->
            distinctTool.switchMap { translationCache.get(TranslationKey(it, locale))!!.withInitialValue(null) }
                .distinctUntilChanged()
                .combineWith(acc.distinctUntilChanged()) { it, translations -> translations + Pair(locale, it) }
        }.map { it.toMap() }
    val state =
        locales.combineWith(manifests, translations, isSyncRunning) { locales, manifests, translations, isSyncRunning ->
            locales.mapIndexed { i, l -> determineState(manifests.getOrNull(i), translations[l], isSyncRunning) }
        }
    // endregion Language Switcher

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
