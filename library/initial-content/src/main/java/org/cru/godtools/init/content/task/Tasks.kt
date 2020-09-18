package org.cru.godtools.init.content.task

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.concurrent.futures.await
import dagger.Reusable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
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
import org.cru.godtools.model.event.LanguageUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber

private const val TAG = "InitialContentTasks"

private const val SYNC_TIME_DEFAULT_TOOLS = "last_synced.default_tools"

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

            eventBus.post(LanguageUpdateEvent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading bundled languages")
        }
    }

    fun initSystemLanguages() {
        if (dao.getCursor(Query.select<Language>().where(LanguageTable.SQL_WHERE_ADDED)).count > 0) return

        // add device languages if we haven't added languages before
        if (dao.get(Query.select<Language>().where(LanguageTable.SQL_WHERE_ADDED).limit(1)).isEmpty()) {
            LocaleUtils.getFallbacks(context.deviceLocale, Locale.ENGLISH).toList()
                // add all device languages and fallbacks
                .onEach { downloadManager.addLanguage(it) }
                .asSequence()
                // set the first available language as the primary language
                .firstOrNull { dao.find<Language>(it) != null }?.let { settings.primaryLanguage = it }

            // always add english and bundled languages
            downloadManager.addLanguage(Locale.ENGLISH)
            BuildConfig.BUNDLED_LANGUAGES.forEach { downloadManager.addLanguage(LocaleCompat.forLanguageTag(it)) }
        }
    }
    // endregion Language Initial Content Tasks

    // region Tool Initial Content Tasks
    suspend fun loadBundledTools() = withContext(Dispatchers.IO) {
        // short-circuit if we already have any tools loaded
        if (dao.getCursor(Tool::class.java).count > 0) return@withContext

        try {
            val tools = context.assets.open("tools.json").reader().use { it.readText() }
                .let { jsonApiConverter.fromJson(it, Tool::class.java) }

            dao.transaction {
                tools.data.forEach { tool ->
                    // if (dao.refresh(tool) != null) return@forEach
                    if (dao.insert(tool, SQLiteDatabase.CONFLICT_IGNORE) == -1L) return@forEach
                    tool.latestTranslations?.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
                    tool.attachments?.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) }
                }
            }

            // send a broadcast for updated objects
            eventBus.post(ToolUpdateEvent)
            eventBus.post(TranslationUpdateEvent)
            eventBus.post(AttachmentUpdateEvent)
        } catch (e: Exception) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag(TAG).e(e, "Error loading bundled tools")
        }
    }

    suspend fun initDefaultTools() {
        // check to see if we have initialized the default tools before
        if (dao.getLastSyncTime(SYNC_TIME_DEFAULT_TOOLS) > 0) return

        // add any bundled tools as the default tools
        BuildConfig.BUNDLED_TOOLS
            .map { downloadManager.addTool(it) }
            .forEach { it.await() }

        dao.updateLastSyncTime(SYNC_TIME_DEFAULT_TOOLS)
    }
    // endregion Tool Initial Content Tasks

    suspend fun importBundledAttachments() = withContext(Dispatchers.IO) {
        try {
            val files = context.assets.list("attachments")?.toSet().orEmpty()

            // find any attachments that aren't downloaded, but came bundled with the resource for
            dao.get(Query.select<Attachment>().where(AttachmentTable.SQL_WHERE_NOT_DOWNLOADED))
                .filter { files.contains(it.localFileName) }
                .forEach { attachment ->
                    launch(Dispatchers.IO) {
                        context.assets.open("attachments/${attachment.localFileName}").use {
                            downloadManager.importAttachment(attachment, it)
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
                                .use { downloadManager.storeTranslation(translation, it, -1) }
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
