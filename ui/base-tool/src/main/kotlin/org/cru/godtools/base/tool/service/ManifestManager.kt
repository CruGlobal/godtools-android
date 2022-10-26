package org.cru.godtools.base.tool.service

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import dagger.Reusable
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.androidx.collection.WeakLruCache
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.kotlin.coroutines.MutexMap
import org.ccci.gto.android.common.kotlin.coroutines.withLock
import org.cru.godtools.model.Translation
import org.cru.godtools.shared.tool.parser.ManifestParser
import org.cru.godtools.shared.tool.parser.ParserResult
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import org.keynote.godtools.android.db.repository.TranslationsRepository

private const val COROUTINES_PARALLELISM = 8

@Reusable
@OptIn(ExperimentalCoroutinesApi::class)
class ManifestManager @Inject constructor(
    private val dao: GodToolsDao,
    private val parser: ManifestParser,
    private val translationsRepository: TranslationsRepository
) {
    private val coroutineDispatcher = Dispatchers.IO.limitedParallelism(COROUTINES_PARALLELISM)
    private val coroutineScope = CoroutineScope(coroutineDispatcher + SupervisorJob())
    private val cache = WeakLruCache<String, ParserResult.Data>(6)
    private val loadingMutex = MutexMap()

    @AnyThread
    fun preloadLatestPublishedManifest(toolCode: String, locale: Locale) {
        coroutineScope.launch {
            val t = translationsRepository.getLatestTranslation(toolCode, locale, isDownloaded = true)
            if (t != null) getManifest(t)
        }
    }

    fun getLatestPublishedManifestFlow(toolCode: String, locale: Locale) =
        translationsRepository.getLatestTranslationFlow(toolCode, locale, isDownloaded = true, trackAccess = true)
            .mapLatest { it?.let { getManifest(it) } }

    @MainThread
    fun getLatestPublishedManifestLiveData(toolCode: String, locale: Locale) =
        translationsRepository.getLatestTranslationLiveData(toolCode, locale, isDownloaded = true, trackAccess = true)
            .switchMap {
                when (it) {
                    null -> emptyLiveData()
                    else -> getManifestLiveData(it)
                }
            }

    private fun getManifestLiveData(translation: Translation) =
        liveData(coroutineDispatcher) { emit(getManifest(translation)) }

    suspend fun getManifest(translation: Translation): Manifest? {
        val manifestFileName = translation.manifestFileName ?: return null
        return when (val result = parseManifest(manifestFileName)) {
            is ParserResult.Error.Corrupted, is ParserResult.Error.NotFound -> {
                withContext(coroutineDispatcher) { brokenManifest(manifestFileName) }
                null
            }
            is ParserResult.Data -> result.manifest
            else -> null
        }
    }

    @AnyThread
    private suspend fun parseManifest(name: String) = loadingMutex.withLock(name) {
        cache[name] ?: withContext(coroutineDispatcher) {
            parser.parseManifest(name).also { if (it is ParserResult.Data) cache.put(name, it) }
        }
    }

    @WorkerThread
    private fun brokenManifest(manifestName: String) {
        dao.update(
            Translation().apply { isDownloaded = false },
            TranslationTable.FIELD_MANIFEST.eq(manifestName),
            TranslationTable.COLUMN_DOWNLOADED
        )
    }
}
