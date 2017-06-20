package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.jsonapi.util.Includes;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.cru.godtools.model.event.LanguageUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.model.Attachment;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

import java.util.List;

abstract class BaseDataSyncTasks extends BaseSyncTasks {
    private static final String[] API_FIELDS_LANGUAGE = {LanguageTable.COLUMN_CODE};
    private static final String[] API_FIELDS_TOOL =
            {ToolTable.COLUMN_NAME, ToolTable.COLUMN_DESCRIPTION, ToolTable.COLUMN_SHARES, ToolTable.COLUMN_BANNER,
                    ToolTable.COLUMN_DETAILS_BANNER, ToolTable.COLUMN_COPYRIGHT};
    private static final String[] API_FIELDS_ATTACHMENT =
            {AttachmentTable.COLUMN_TOOL, AttachmentTable.COLUMN_FILENAME, AttachmentTable.COLUMN_SHA256};
    private static final String[] API_FIELDS_TRANSLATION =
            {TranslationTable.COLUMN_TOOL, TranslationTable.COLUMN_LANGUAGE, TranslationTable.COLUMN_VERSION,
                    TranslationTable.COLUMN_NAME, TranslationTable.COLUMN_DESCRIPTION, TranslationTable.COLUMN_MANIFEST,
                    TranslationTable.COLUMN_PUBLISHED};

    BaseDataSyncTasks(@NonNull Context context) {
        super(context);
    }

    void storeLanguage(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final Language language) {
        mDao.updateOrInsert(language, API_FIELDS_LANGUAGE);
        coalesceEvent(events, new LanguageUpdateEvent());
    }

    void storeTools(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final List<Tool> tools,
                    @Nullable final LongSparseArray<Tool> existing, @NonNull final Includes includes) {
        for (final Tool tool : tools) {
            if (existing != null) {
                existing.remove(tool.getId());
            }
            storeTool(events, tool, includes);
        }

        // prune any existing tools that weren't synced and aren't already added to the device
        if (existing != null) {
            for (int i = 0; i < existing.size(); i++) {
                final Tool tool = existing.valueAt(i);
                if (!tool.isAdded()) {
                    mDao.delete(tool);
                    coalesceEvent(events, new ToolUpdateEvent());

                    // delete any attachments for this tool
                    mDao.delete(Attachment.class, AttachmentTable.FIELD_TOOL.eq(tool.getId()));
                }
            }
        }
    }

    private void storeTool(@NonNull final SimpleArrayMap<Class<?>, Object> events, @NonNull final Tool tool,
                           @NonNull final Includes includes) {
        mDao.updateOrInsert(tool, API_FIELDS_TOOL);
        coalesceEvent(events, new ToolUpdateEvent());

        // persist any related included objects
        if (includes.include(Tool.JSON_LATEST_TRANSLATIONS)) {
            final List<Translation> translations = tool.getLatestTranslations();
            if (translations != null) {
                storeTranslations(events, translations, includes.descendant(Tool.JSON_LATEST_TRANSLATIONS));
            }
        }
        if (includes.include(Tool.JSON_ATTACHMENTS)) {
            final List<Attachment> attachments = tool.getAttachments();
            if (attachments != null) {
                final LongSparseArray<Attachment> existing = index(mDao.get(
                        Query.select(Attachment.class).where(AttachmentTable.FIELD_TOOL.eq(tool.getId()))));
                storeAttachments(events, attachments, existing, includes.descendant(Tool.JSON_ATTACHMENTS));
            }
        }
    }

    private void storeTranslations(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                   @NonNull final List<Translation> translations, @NonNull final Includes includes) {
        for (final Translation translation : translations) {
            storeTranslation(events, translation, includes);
        }
    }

    private void storeTranslation(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                  @NonNull final Translation translation, @NonNull final Includes includes) {
        mDao.updateOrInsert(translation, API_FIELDS_TRANSLATION);
        coalesceEvent(events, new TranslationUpdateEvent());

        if (includes.include(Translation.JSON_LANGUAGE)) {
            final Language language = translation.getLanguage();
            if (language != null) {
                storeLanguage(events, language);
            }
        }
    }

    private void storeAttachments(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                  @NonNull final List<Attachment> attachments,
                                  @Nullable final LongSparseArray<Attachment> existing,
                                  @NonNull final Includes includes) {
        for (final Attachment attachment : attachments) {
            if (existing != null) {
                existing.remove(attachment.getId());
            }
            storeAttachment(events, attachment, includes);
        }

        // prune any existing attachments that weren't synced
        if (existing != null) {
            for (int i = 0; i < existing.size(); i++) {
                final Attachment attachment = existing.valueAt(i);
                mDao.delete(attachment);
                coalesceEvent(events, new AttachmentUpdateEvent());
            }
        }
    }

    private void storeAttachment(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                 @NonNull final Attachment attachment, @NonNull final Includes includes) {
        mDao.updateOrInsert(attachment, API_FIELDS_ATTACHMENT);
        coalesceEvent(events, new AttachmentUpdateEvent());
    }
}
