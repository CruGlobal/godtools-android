package org.cru.godtools.tract.activity

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchFold
import org.ccci.gto.android.common.androidx.lifecycle.withInitialValue
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.model.Translation
import org.cru.godtools.model.TranslationKey
import org.cru.godtools.xml.model.Manifest
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

class TractActivityDataModel @AssistedInject constructor(
    private val dao: GodToolsDao,
    private val manifestManager: ManifestManager,
    @Assisted private val savedState: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<TractActivityDataModel>

    val tool = MutableLiveData<String?>()
    val locales = MutableLiveData<List<Locale>>(emptyList())

    private val distinctTool = tool.distinctUntilChanged()
    private val distinctLocales = locales.distinctUntilChanged()

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
