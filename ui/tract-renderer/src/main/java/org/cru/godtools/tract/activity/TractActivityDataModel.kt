package org.cru.godtools.tract.activity

import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
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
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_INVALID_TYPE
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADED
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_LOADING
import org.cru.godtools.base.tool.activity.BaseToolActivity.Companion.STATE_NOT_FOUND
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
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
    val locales = MutableLiveData<List<Locale>>(emptyList())
    val isSyncRunning = MutableLiveData<Boolean?>(null)
    private val distinctTool = tool.distinctUntilChanged()
    private val distinctLocales = locales.distinctUntilChanged()

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
    val activeTranslation = distinctTool.switchCombineWith(activeLocale) { t, l ->
        translationCache.get(TranslationKey(t, l))!!.withInitialValue(null)
    }
    val activeState = rawActiveManifest.combineWith(activeTranslation, isSyncRunning) { m, t, s ->
        determineState(m, t, s)
    }
    // endregion Active Tool

    val downloadProgress = distinctTool.switchCombineWith(activeLocale) { t, l ->
        when {
            t == null || l == null -> emptyLiveData()
            else -> downloadManager.getDownloadProgressLiveData(t, l)
        }
    }

    val manifests: LiveData<List<Manifest?>> =
        distinctLocales.switchFold(ImmutableLiveData(emptyList())) { acc, locale ->
            val manifest =
                distinctTool.switchMap { manifestCache.get(TranslationKey(it, locale))!!.withInitialValue(null) }
                    .distinctUntilChanged()
            acc.distinctUntilChanged().combineWith(manifest) { manifests, manifest -> manifests + manifest }
        }
    val translations: LiveData<List<Translation?>> =
        distinctLocales.switchFold(ImmutableLiveData(emptyList())) { acc, locale ->
            val translation =
                distinctTool.switchMap { translationCache.get(TranslationKey(it, locale))!!.withInitialValue(null) }
                    .distinctUntilChanged()
            acc.distinctUntilChanged().combineWith(translation) { translations, trans -> translations + trans }
        }
    val state = manifests.combineWith(translations, isSyncRunning) { manifests, translations, isSyncRunning ->
        manifests.mapIndexed { i, manifest -> determineState(manifest, translations.getOrNull(i), isSyncRunning) }
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

@VisibleForTesting(otherwise = PRIVATE)
internal fun determineState(manifest: Manifest?, translation: Translation?, isSyncRunning: Boolean?) = when {
    manifest != null && manifest.type != Manifest.Type.TRACT -> STATE_INVALID_TYPE
    manifest != null -> STATE_LOADED
    translation == null && isSyncRunning == false -> STATE_NOT_FOUND
    else -> STATE_LOADING
}
