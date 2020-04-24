package org.cru.godtools.init.content.task

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
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
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

private const val TAG = "InitialContentTasks"

internal class Tasks @Inject constructor(
    private val context: Context,
    private val dao: GodToolsDao,
    private val downloadManager: GodToolsDownloadManager,
    private val settings: Settings,
    private val eventBus: EventBus
) {
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addClasses(Tool::class.java, Translation::class.java, Attachment::class.java)
            .addConverters(ToolTypeConverter)
            .addConverters(LocaleTypeConverter())
            .build()
    }

    // region Language Initial Content Tasks
    suspend fun loadBundledLanguages() =  withContext(Dispatchers.IO) {
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
        } catch (e: java.lang.Exception) {
            // log exception, but it shouldn't be fatal (for now)
            Timber.tag(TAG).e(e, "Error loading bundled tools")
        }
    }
    // endregion Tool Initial Content Tasks
}
