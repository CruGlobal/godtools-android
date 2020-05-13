package org.cru.godtools.tract.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.androidx.lifecycle.switchFold
import org.ccci.gto.android.common.androidx.lifecycle.withInitialValue
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.xml.model.Manifest
import java.util.Locale

class TractActivityDataModel @AssistedInject constructor(
    private val manifestManager: ManifestManager,
    @Assisted private val savedState: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<TractActivityDataModel>

    val tool = MutableLiveData<String?>()
    val locales = MutableLiveData<List<Locale>>(emptyList())

    val manifests =
        locales.distinctUntilChanged().switchFold(MutableLiveData(emptyList<Manifest?>())) { manifests, locale ->
            val manifest = tool.distinctUntilChanged().switchMap {
                when {
                    it != null -> manifestManager.getLatestPublishedManifestLiveData(it, locale).withInitialValue(null)
                    else -> emptyLiveData()
                }
            }
            manifests.distinctUntilChanged().combineWith(manifest.distinctUntilChanged()) { list, item -> list + item }
        }
}
