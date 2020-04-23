package org.cru.godtools.init.content.task

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.LanguageUpdateEvent
import org.cru.godtools.model.jsonapi.ToolTypeConverter
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "InitialContentTasks"

internal class Tasks @Inject constructor(
    private val context: Context,
    private val dao: GodToolsDao,
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

    suspend fun loadBundledLanguages() {
        // short-circuit if we already have any languages loaded
        if (dao.get(Query.select<Language>().limit(1)).isNotEmpty()) return

        try {
            val languages = withContext(Dispatchers.IO) {
                context.assets.open("languages.json").reader().use { it.readText() }
                    .let { jsonApiConverter.fromJson(it, Language::class.java) }
            }

            dao.transaction { languages.data.forEach { dao.insert(it, SQLiteDatabase.CONFLICT_IGNORE) } }

            eventBus.post(LanguageUpdateEvent)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading bundled languages")
        }
    }
}
