package org.cru.godtools.sync.task

import android.database.sqlite.SQLiteDatabase
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.collection.LongSparseArray
import androidx.collection.SimpleArrayMap
import androidx.collection.forEach
import androidx.collection.valueIterator
import java.util.Locale
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Base
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.keynote.godtools.android.db.Contract.AttachmentTable
import org.keynote.godtools.android.db.Contract.LanguageTable
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class BaseDataSyncTasks internal constructor(protected val dao: GodToolsDao, eventBus: EventBus) :
    BaseSyncTasks(eventBus) {
    // region Tools
    protected fun storeTools(
        events: SimpleArrayMap<Class<*>, Any>,
        tools: List<Tool>,
        existing: LongSparseArray<Tool>?,
        includes: Includes
    ) {
        tools.forEach {
            storeTool(events, it, includes)
            existing?.remove(it.id)
        }

        // prune any existing tools that weren't synced and aren't already added to the device
        existing?.forEach { _, tool ->
            if (tool.isAdded) return@forEach

            dao.delete(tool)
            coalesceEvent(events, ToolUpdateEvent)

            // delete any attachments for this tool
            dao.delete(Attachment::class.java, AttachmentTable.FIELD_TOOL.eq(tool.id))
        }
    }

    private fun storeTool(events: SimpleArrayMap<Class<*>, Any>, tool: Tool, includes: Includes) {
        dao.updateOrInsert(
            tool, SQLiteDatabase.CONFLICT_REPLACE,
            ToolTable.COLUMN_CODE, ToolTable.COLUMN_TYPE, ToolTable.COLUMN_NAME, ToolTable.COLUMN_DESCRIPTION,
            ToolTable.COLUMN_CATEGORY, ToolTable.COLUMN_SHARES, ToolTable.COLUMN_BANNER,
            ToolTable.COLUMN_DETAILS_BANNER, ToolTable.COLUMN_DETAILS_BANNER_ANIMATION,
            ToolTable.COLUMN_DETAILS_BANNER_YOUTUBE, ToolTable.COLUMN_DEFAULT_ORDER, ToolTable.COLUMN_HIDDEN,
            ToolTable.COLUMN_SCREEN_SHARE_DISABLED, ToolTable.COLUMN_SPOTLIGHT
        )
        coalesceEvent(events, ToolUpdateEvent)

        // persist related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) tool.latestTranslations?.let { translations ->
            storeTranslations(
                events, translations,
                includes = includes.descendant(Tool.JSON_LATEST_TRANSLATIONS),
                existing = tool.code?.let { code ->
                    index(Query.select<Translation>().where(TranslationTable.FIELD_TOOL.eq(code)).get(dao))
                }
            )
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) tool.attachments?.let { attachments ->
            storeAttachments(
                attachments,
                existing = index(Query.select<Attachment>().where(AttachmentTable.FIELD_TOOL.eq(tool.id)).get(dao))
            )
        }
    }
    // endregion Tools

    // region Languages
    protected fun storeLanguages(languages: List<Language>, existing: MutableMap<Locale, Language>?) {
        languages.filter { it.isValid }.forEach {
            storeLanguage(it)
            existing?.remove(it.code)
        }

        // prune any existing languages that weren't synced and aren't already added to the device
        existing?.values?.forEach { dao.delete(it) }
    }

    @VisibleForTesting
    internal fun storeLanguage(language: Language) {
        if (!language.isValid) return

        // this language doesn't exist yet, check to see if a different language shares the same id
        if (language.id != Base.INVALID_ID && dao.refresh(language) == null) {
            // update the language code to preserve the added state
            Query.select<Language>().where(LanguageTable.FIELD_ID.eq(language.id)).limit(1)
                .get(dao)
                .firstOrNull()
                ?.let { old ->
                    dao.update(language, dao.getPrimaryKeyWhere(old), LanguageTable.COLUMN_CODE)
                }
        }

        dao.updateOrInsert(
            language, SQLiteDatabase.CONFLICT_REPLACE,
            LanguageTable.COLUMN_ID, LanguageTable.COLUMN_NAME
        )
    }
    // endregion Languages

    // region Translations
    private fun storeTranslations(
        events: SimpleArrayMap<Class<*>, Any>,
        translations: List<Translation>,
        existing: LongSparseArray<Translation>?,
        includes: Includes
    ) {
        translations.forEach {
            storeTranslation(events, it, includes)
            existing?.remove(it.id)
        }

        // prune any existing translations that weren't synced and aren't downloaded to the device
        existing?.forEach { _, translation ->
            dao.refresh(translation)?.takeUnless { it.isDownloaded }?.let {
                dao.delete(it)
                coalesceEvent(events, TranslationUpdateEvent)
            }
        }
    }

    private fun storeTranslation(events: SimpleArrayMap<Class<*>, Any>, translation: Translation, includes: Includes) {
        dao.updateOrInsert(
            translation,
            TranslationTable.COLUMN_TOOL, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
            TranslationTable.COLUMN_NAME, TranslationTable.COLUMN_DESCRIPTION, TranslationTable.COLUMN_TAGLINE,
            TranslationTable.COLUMN_MANIFEST, TranslationTable.COLUMN_PUBLISHED
        )
        coalesceEvent(events, TranslationUpdateEvent)

        if (includes.include(Translation.JSON_LANGUAGE)) translation.language?.let { storeLanguage(it) }
    }
    // endregion Translations

    // region Attachments
    private fun storeAttachments(attachments: List<Attachment>, existing: LongSparseArray<Attachment>?) {
        attachments.forEach {
            storeAttachment(it)
            existing?.remove(it.id)
        }

        // prune any existing attachments that weren't synced
        existing?.valueIterator()?.forEach { dao.delete(it) }
    }

    private fun storeAttachment(attachment: Attachment) = dao.updateOrInsert(
        attachment,
        AttachmentTable.COLUMN_TOOL, AttachmentTable.COLUMN_FILENAME, AttachmentTable.COLUMN_SHA256
    )
    // endregion Attachments
}
