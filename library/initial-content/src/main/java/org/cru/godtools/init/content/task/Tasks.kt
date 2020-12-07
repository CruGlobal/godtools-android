package org.cru.godtools.init.content.task

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.base.Settings
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.init.content.BuildConfig
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
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
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val jsonApiConverter: JsonApiConverter,
    private val settings: Settings,
    private val eventBus: EventBus
) {
    // region Language Initial Content Tasks
    suspend fun loadBundledLanguages() = withContext(Dispatchers.IO) {
        // short-circuit if we already have any languages loaded
        if (dao.getCursor(Language::class.java).count > 0) return@withContext

        try {
            val languages = context.assets.open("languages.json").reader().use { it.readText() }
                .let { jsonApiConverter.fromJson(it, Language::class.java) }

            dao.transaction { languages.data.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) } }
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading bundled languages")
        }
    }

    suspend fun initSystemLanguages() {
        if (dao.getCursor(Query.select<Language>().where(LanguageTable.SQL_WHERE_ADDED)).count > 0) return

        // add device languages if we haven't added languages before
        if (dao.get(Query.select<Language>().where(LanguageTable.SQL_WHERE_ADDED).limit(1)).isEmpty()) {
            coroutineScope {
                LocaleUtils.getFallbacks(context.deviceLocale, Locale.ENGLISH).toList()
                    // add all device languages and fallbacks
                    .onEach { launch { downloadManager.pinLanguage(it) } }
                    .asSequence()
                    // set the first available language as the primary language
                    .firstOrNull { dao.find<Language>(it) != null }?.let { settings.primaryLanguage = it }

                // always add english and bundled languages
                launch { downloadManager.pinLanguage(Locale.ENGLISH) }
                BuildConfig.BUNDLED_LANGUAGES.forEach {
                    launch { downloadManager.pinLanguage(LocaleCompat.forLanguageTag(it)) }
                }
            }
        }
    }
    // endregion Language Initial Content Tasks

    // region Tool Initial Content Tasks
    suspend fun loadBundledTools() = withContext(Dispatchers.IO) {
        // short-circuit if we already have any tools loaded
        if (dao.getCursor(Tool::class.java).count > 0) return@withContext

        bundledTools.let { tools ->
            dao.transaction {
                tools.forEach { tool ->
                    if (dao.insert(tool, SQLiteDatabase.CONFLICT_IGNORE) == -1L) return@forEach
                    tool.latestTranslations?.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
                    tool.attachments?.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
                }
            }
        }

        // send a broadcast for updated objects
        eventBus.post(ToolUpdateEvent)
        eventBus.post(TranslationUpdateEvent)
        eventBus.post(AttachmentUpdateEvent)
    }

    suspend fun initDefaultTools() {
        // check to see if we have initialized the default tools before
        if (dao.getLastSyncTime(SYNC_TIME_DEFAULT_TOOLS) > 0) return

        // add any bundled tools as the default tools
        coroutineScope {
            BuildConfig.BUNDLED_TOOLS
                .map { launch { downloadManager.pinTool(it) } }
                .joinAll()
        }

        dao.updateLastSyncTime(SYNC_TIME_DEFAULT_TOOLS)
    }

    suspend fun initFavoriteTools() {
        // check to see if we have initialized the default tools before
        if (dao.getLastSyncTime(SYNC_TIME_DEFAULT_TOOLS) > 0) return

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
                .map { launch { downloadManager.pinTool(it) } }
                .toList().joinAll()
        }

        dao.updateLastSyncTime(SYNC_TIME_DEFAULT_TOOLS)
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
            dao.get(Query.select<Attachment>().where(AttachmentTable.SQL_WHERE_NOT_DOWNLOADED))
                .filter { files.contains(it.localFilename) }
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

                    // ensure the tool and language are added to this device
                    val toolCode = translation.toolCode ?: return@launch
                    val tool = dao.find<Tool>(toolCode)?.takeIf { it.isAdded } ?: return@launch
                    val languageCode = translation.languageCode
                    val language = dao.find<Language>(languageCode)?.takeIf { it.isAdded } ?: return@launch

                    // short-circuit if a newer translation is already downloaded
                    val latestTranslation =
                        dao.getLatestTranslation(toolCode, languageCode, isPublished = true, isDownloaded = true)
                    if (latestTranslation != null && latestTranslation.version >= translation.version) return@launch

                    withContext(Dispatchers.IO) {
                        try {
                            context.assets.open("translations/$file")
                                .use { downloadManager.importTranslation(translation, it, -1) }
                        } catch (e: IOException) {
                            Timber.tag(TAG).e(
                                e, "Error importing bundled translation %s-%s-%d (%s)", tool.code, language.code,
                                translation.version, file
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
