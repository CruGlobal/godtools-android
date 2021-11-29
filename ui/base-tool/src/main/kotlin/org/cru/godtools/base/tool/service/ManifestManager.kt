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
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.ccci.gto.android.common.support.v4.util.WeakLruCache
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.cru.godtools.tool.FEATURE_ANIMATION
import org.cru.godtools.tool.FEATURE_CONTENT_CARD
import org.cru.godtools.tool.FEATURE_FLOW
import org.cru.godtools.tool.FEATURE_MULTISELECT
import org.cru.godtools.tool.ParserConfig
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.service.ManifestParser
import org.cru.godtools.tool.service.ParserResult
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Reusable
class ManifestManager @Inject constructor(
    private val dao: GodToolsDao,
    private val eventBus: EventBus,
    private val parser: ManifestParser
) {
    private val cache = WeakLruCache<String, ParserResult.Data>(6)
    private val loadingMutex = MutexMap()

    init {
        ParserConfig.supportedFeatures =
            setOf(FEATURE_ANIMATION, FEATURE_CONTENT_CARD, FEATURE_FLOW, FEATURE_MULTISELECT)
    }

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
        return when (val result = parseManifest(manifestFileName)) {
            is ParserResult.Error.Corrupted, is ParserResult.Error.NotFound -> {
                withContext(Dispatchers.Default) { brokenManifest(manifestFileName) }
                null
            }
            is ParserResult.Data -> result.manifest
            else -> null
        }
    }

    @AnyThread
    private suspend fun parseManifest(name: String) = loadingMutex.withLock(name) {
        cache[name] ?: parser.parseManifest(name).also { if (it is ParserResult.Data) cache.put(name, it) }
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
