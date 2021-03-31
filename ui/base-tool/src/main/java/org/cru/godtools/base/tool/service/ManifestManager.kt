package org.cru.godtools.base.tool.service

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.Reusable
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.service.ManifestParser
import org.cru.godtools.xml.service.Result
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Reusable
class ManifestManager @Inject constructor(
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val manifestParser: ManifestParser
) {
    @AnyThread
    fun preloadLatestPublishedManifest(toolCode: String, locale: Locale) {
        GlobalScope.launch(Dispatchers.Default) {
            val t = dao.getLatestTranslation(toolCode, locale, isPublished = true, isDownloaded = true)
            if (t != null) getManifest(t)
        }
    }

    @MainThread
    fun getLatestPublishedManifestLiveData(toolCode: String, locale: Locale) =
        dao.getLatestTranslationLiveData(toolCode, locale, isDownloaded = true, trackAccess = true)
            .switchMap {
                when (it) {
                    null -> emptyLiveData()
                    else -> getManifestLiveData(it)
                }
            }

    private fun getManifestLiveData(translation: Translation) = liveData { emit(getManifest(translation)) }

    suspend fun getManifest(translation: Translation): Manifest? {
        val manifestFileName = translation.manifestFileName ?: return null
        val toolCode = translation.toolCode ?: return null
        return when (val result = manifestParser.parse(manifestFileName, toolCode, translation.languageCode)) {
            is Result.Error.Corrupted, is Result.Error.NotFound -> {
                withContext(Dispatchers.Default) { brokenManifest(manifestFileName) }
                null
            }
            is Result.Data -> result.manifest
            else -> null
        }
    }

    @WorkerThread
    private fun brokenManifest(manifestName: String) {
        dao.update(
            Translation().apply { isDownloaded = false },
            TranslationTable.FIELD_MANIFEST.eq(manifestName),
            TranslationTable.COLUMN_DOWNLOADED
        )
        eventBus.post(TranslationUpdateEvent)
    }
}
