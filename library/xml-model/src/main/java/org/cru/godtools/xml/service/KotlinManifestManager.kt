package org.cru.godtools.xml.service

import android.content.Context
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.lifecycle.emptyLiveData
import org.ccci.gto.android.common.lifecycle.observeOnce
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.cru.godtools.xml.model.Manifest
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import java.util.Locale

open class KotlinManifestManager(@JvmField protected val context: Context) {
    @JvmField
    protected val dao = GodToolsDao.getInstance(context)

    @JvmField
    protected val manifestParser = ManifestParser.getInstance(context)

    @MainThread
    fun getLatestPublishedManifestLiveData(toolCode: String, locale: Locale) =
        dao.getLatestTranslationLiveData(toolCode, locale, isDownloaded = true)
            .apply {
                observeOnce {
                    if (it != null) {
                        it.updateLastAccessed()
                        dao.update(it, TranslationTable.COLUMN_LAST_ACCESSED)
                    }
                }
            }
            .switchMap {
                when (it) {
                    null -> emptyLiveData()
                    else -> getManifestLiveData(it)
                }
            }

    fun getManifestLiveData(translation: Translation): LiveData<Manifest?> {
        val manifestFileName = translation.manifestFileName ?: return emptyLiveData()
        val toolCode = translation.toolCode ?: return emptyLiveData()
        return liveData {
            when (val result = manifestParser.parse(manifestFileName, toolCode, translation.languageCode)) {
                is Result.Error.Corrupted, is Result.Error.NotFound -> {
                    withContext(Dispatchers.Default) { brokenManifest(manifestFileName) }
                    emit(null)
                }
                is Result.Data -> emit(result.manifest)
                else -> emit(null)
            }
        }
    }

    @WorkerThread
    protected open fun brokenManifest(manifestName: String) {
        dao.update(
            Translation().apply { isDownloaded = false },
            TranslationTable.FIELD_MANIFEST.eq(manifestName),
            TranslationTable.COLUMN_DOWNLOADED
        )
        EventBus.getDefault().post(TranslationUpdateEvent)
    }
}
