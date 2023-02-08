package org.cru.godtools.sync.repository

import android.database.sqlite.SQLiteDatabase
import androidx.annotation.VisibleForTesting
import androidx.collection.LongSparseArray
import androidx.collection.forEach
import androidx.collection.valueIterator
import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.jsonapi.util.Includes
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.sync.task.BaseSyncTasks
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.Contract.TranslationTable
import org.keynote.godtools.android.db.GodToolsDao

@Singleton
internal class SyncRepository @Inject constructor(
    private val attachmentsRepository: AttachmentsRepository,
    private val dao: GodToolsDao,
    private val languagesRepository: LanguagesRepository,
) {
    // region Tools
    fun storeTools(tools: List<Tool>, existingTools: LongSparseArray<Tool>?, includes: Includes) {
        tools.forEach {
            storeTool(it, existingTools, includes)
            existingTools?.remove(it.id)
        }

        // prune any existing tools that weren't synced and aren't already added to the device
        existingTools?.forEach { _, tool ->
            if (tool.isAdded) return@forEach

            dao.delete(tool)

            // delete any orphaned objects for this tool
            attachmentsRepository.deleteAttachmentsFor(tool)
            tool.code?.let { dao.delete(Translation::class.java, TranslationTable.FIELD_TOOL.eq(it)) }
        }
    }

    private fun storeTool(tool: Tool, existingTools: LongSparseArray<Tool>?, includes: Includes) {
        // don't store the tool if it's not valid
        if (!tool.isValid) return

        dao.updateOrInsert(
            tool, SQLiteDatabase.CONFLICT_REPLACE,
            ToolTable.COLUMN_CODE, ToolTable.COLUMN_TYPE, ToolTable.COLUMN_NAME, ToolTable.COLUMN_DESCRIPTION,
            ToolTable.COLUMN_CATEGORY, ToolTable.COLUMN_SHARES, ToolTable.COLUMN_BANNER,
            ToolTable.COLUMN_DETAILS_BANNER, ToolTable.COLUMN_DETAILS_BANNER_ANIMATION,
            ToolTable.COLUMN_DETAILS_BANNER_YOUTUBE, ToolTable.COLUMN_DEFAULT_ORDER, ToolTable.COLUMN_HIDDEN,
            ToolTable.COLUMN_SCREEN_SHARE_DISABLED, ToolTable.COLUMN_SPOTLIGHT, ToolTable.COLUMN_META_TOOL,
            ToolTable.COLUMN_DEFAULT_VARIANT
        )

        // persist related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) tool.latestTranslations?.let { translations ->
            storeTranslations(
                translations,
                includes = includes.descendant(Tool.JSON_LATEST_TRANSLATIONS),
                existing = tool.code?.let { code ->
                    BaseSyncTasks.index(
                        Query.select<Translation>().where(TranslationTable.FIELD_TOOL.eq(code)).get(dao)
                    )
                }
            )
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) tool.attachments?.let { attachments ->
            attachmentsRepository.storeAttachmentsFromSync(attachments)
            attachmentsRepository.removeAttachmentsMissingFromSync(tool.id, attachments)
        }
        if (includes.include(Tool.JSON_METATOOL)) {
            tool.metatool?.let {
                storeTool(it, existingTools, includes.descendant(Tool.JSON_METATOOL))
                existingTools?.remove(it.id)
            }
        }
        if (includes.include(Tool.JSON_DEFAULT_VARIANT)) {
            tool.defaultVariant?.let {
                storeTool(it, existingTools, includes.descendant(Tool.JSON_DEFAULT_VARIANT))
                existingTools?.remove(it.id)
            }
        }
    }
    // endregion Tools

    // region Languages
    @VisibleForTesting
    fun storeLanguage(language: Language) {
        if (!language.isValid) return
        languagesRepository.storeLanguageFromSync(language)
    }
    // endregion Languages

    // region Translations
    private fun storeTranslations(
        translations: List<Translation>,
        existing: LongSparseArray<Translation>?,
        includes: Includes
    ) {
        translations.forEach {
            storeTranslation(it, includes)
            existing?.remove(it.id)
        }

        // prune any existing translations that weren't synced and aren't downloaded to the device
        existing?.valueIterator()?.forEach { translation ->
            dao.refresh(translation)?.takeUnless { it.isDownloaded }?.let { dao.delete(it) }
        }
    }

    private fun storeTranslation(translation: Translation, includes: Includes) {
        dao.updateOrInsert(
            translation,
            TranslationTable.COLUMN_TOOL, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
            TranslationTable.COLUMN_NAME, TranslationTable.COLUMN_DESCRIPTION, TranslationTable.COLUMN_TAGLINE,
            TranslationTable.COLUMN_DETAILS_OUTLINE, TranslationTable.COLUMN_DETAILS_BIBLE_REFERENCES,
            TranslationTable.COLUMN_DETAILS_CONVERSATION_STARTERS, TranslationTable.COLUMN_MANIFEST,
            TranslationTable.COLUMN_PUBLISHED
        )

        if (includes.include(Translation.JSON_LANGUAGE)) translation.language?.let { storeLanguage(it) }
    }
    // endregion Translations
}