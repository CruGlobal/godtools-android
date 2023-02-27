package org.cru.godtools.init.content.task

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.Settings
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.LastSyncTimeRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber

private const val TAG = "InitialContentTasks"

private const val SYNC_TIME_DEFAULT_TOOLS = "last_synced.default_tools"

@VisibleForTesting
internal const val NUMBER_OF_FAVORITES = 4

@Reusable
internal class Tasks @Inject constructor(
    @ApplicationContext private val context: Context,
    private val attachmentsRepository: AttachmentsRepository,
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val jsonApiConverter: JsonApiConverter,
    private val languagesRepository: LanguagesRepository,
    private val lastSyncTimeRepository: LastSyncTimeRepository,
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository
) {
    // region Language Initial Content Tasks
    suspend fun loadBundledLanguages() {
        // short-circuit if we already have any languages loaded
        if (languagesRepository.getLanguages().isNotEmpty()) return

        try {
            val languages = withContext(Dispatchers.IO) {
                context.assets.open("languages.json").reader().use { it.readText() }
                    .let { jsonApiConverter.fromJson(it, Language::class.java) }
            }

            languagesRepository.storeInitialLanguages(languages.data)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading bundled languages")
        }
    }

    suspend fun initPrimaryLanguage() {
        if (settings.isPrimaryLanguageSet) return

        sequenceOf(context.deviceLocale).filterNotNull().includeFallbacks().distinct()
            // set the first available language as the primary language
            .firstOrNull { languagesRepository.findLanguage(it) != null }?.let { settings.primaryLanguage = it }
    }
    // endregion Language Initial Content Tasks

    // region Tool Initial Content Tasks
    suspend fun loadBundledResources() = withContext(Dispatchers.IO) {
        // short-circuit if we already have any resources loaded
        if (toolsRepository.getResources().isNotEmpty()) return@withContext

        bundledTools.let { resources ->
            toolsRepository.storeInitialResources(resources)
            attachmentsRepository.storeInitialAttachments(resources.flatMap { it.attachments.orEmpty() })
            dao.transaction {
                resources.flatMap { it.latestTranslations.orEmpty() }.forEach { translation ->
                    dao.insert(translation, SQLiteDatabase.CONFLICT_IGNORE)
                }
            }
        }
    }

    suspend fun initFavoriteTools() {
        // check to see if we have initialized the default tools before
        if (lastSyncTimeRepository.getLastSyncTime(SYNC_TIME_DEFAULT_TOOLS) > 0) return
        if (toolsRepository.getTools().any { it.isAdded }) return

        coroutineScope {
            val preferred = async {
                bundledTools.sortedBy { it.initialFavoritesPriority ?: Int.MAX_VALUE }.mapNotNull { it.code }
            }
            val available = Query.select<Translation>()
                .where(
                    TranslationTable.FIELD_LANGUAGE.eq(settings.primaryLanguage)
                        .and(TranslationTable.SQL_WHERE_PUBLISHED)
                )
                .get(dao)
                .mapNotNullTo(mutableSetOf()) { it.toolCode }

            (preferred.await().asSequence().filter { available.contains(it) } + preferred.await().asSequence())
                .distinct()
                .take(NUMBER_OF_FAVORITES)
                .map { launch { toolsRepository.pinTool(it) } }
                .toList().joinAll()
        }

        lastSyncTimeRepository.updateLastSyncTime(SYNC_TIME_DEFAULT_TOOLS)
    }

    private val bundledTools: List<Tool>
        get() = try {
            context.assets.open("tools.json").reader().use { it.readText() }
                .let { jsonApiConverter.fromJson(it, Tool::class.java) }
                .data
        } catch (e: Exception) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag(TAG).e(e, "Error parsing bundled tools")
            emptyList()
        }
    // endregion Tool Initial Content Tasks

    suspend fun importBundledAttachments() = withContext(Dispatchers.IO) {
        try {
            val files = context.assets.list("attachments")?.toSet().orEmpty()

            // find any attachments that aren't downloaded, but came bundled with the resource for
            attachmentsRepository.getAttachments()
                .filter { !it.isDownloaded && it.localFilename in files }
                .forEach { attachment ->
                    launch(Dispatchers.IO) {
                        context.assets.open("attachments/${attachment.localFilename}").use {
                            downloadManager.importAttachment(attachment.id, data = it)
                        }
                    }
                }
        } catch (e: IOException) {
            Timber.tag(TAG).e(e, "Error importing bundled attachments")
        }
    }

    suspend fun importBundledTranslations() = try {
        withContext(Dispatchers.IO) {
            context.assets.list("translations")?.forEach { file ->
                launch {
                    // load the translation unless it's downloaded already
                    val id = file.substring(0, file.lastIndexOf('.'))
                    val translation = dao.find<Translation>(id)?.takeUnless { it.isDownloaded } ?: return@launch

                    // short-circuit if a newer translation is already downloaded
                    val toolCode = translation.toolCode ?: return@launch
                    val languageCode = translation.languageCode
                    val latestTranslation =
                        translationsRepository.findLatestTranslation(toolCode, languageCode, isDownloaded = true)
                    if (latestTranslation != null && latestTranslation.version >= translation.version) return@launch

                    withContext(Dispatchers.IO) {
                        try {
                            context.assets.open("translations/$file")
                                .use { downloadManager.importTranslation(translation, it, -1) }
                        } catch (e: IOException) {
                            Timber.tag(TAG).e(
                                e,
                                "Error importing bundled translation %s-%s-%d (%s)",
                                toolCode,
                                languageCode,
                                translation.version,
                                file
                            )
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Error importing bundled translations")
    }
}
