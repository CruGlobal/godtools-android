package org.keynote.godtools.android.sync;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SimpleArrayMap;

import org.ccci.gto.android.common.jsonapi.util.Includes;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.event.AttachmentUpdateEvent;
import org.keynote.godtools.android.event.LanguageUpdateEvent;
import org.keynote.godtools.android.event.ToolUpdateEvent;
import org.keynote.godtools.android.event.TranslationUpdateEvent;
import org.keynote.godtools.android.model.Attachment;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

import java.util.List;

abstract class BaseDataSyncTasks extends BaseSyncTasks {
    private static final String[] API_FIELDS_LANGUAGE = {LanguageTable.COLUMN_CODE};
    private static final String[] API_FIELDS_TOOL =
            {ToolTable.COLUMN_NAME, ToolTable.COLUMN_DESCRIPTION, ToolTable.COLUMN_SHARES, ToolTable.COLUMN_BANNER,
                    ToolTable.COLUMN_COPYRIGHT};
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

        // prune any existing resources that weren't synced and aren't already added to the device
        if (existing != null) {
            for (int i = 0; i < existing.size(); i++) {
                final Tool tool = existing.valueAt(i);
                if (!tool.isAdded()) {
                    mDao.delete(tool);
                    coalesceEvent(events, new ToolUpdateEvent());
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
                storeAttachments(events, attachments, includes.descendant(Tool.JSON_ATTACHMENTS));
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
                                  @NonNull final List<Attachment> attachments, @NonNull final Includes includes) {
        for (final Attachment attachment : attachments) {
            storeAttachment(events, attachment, includes);
        }
    }

    private void storeAttachment(@NonNull final SimpleArrayMap<Class<?>, Object> events,
                                 @NonNull final Attachment att, @NonNull final Includes includes) {
        mDao.updateOrInsert(att, API_FIELDS_ATTACHMENT);
        coalesceEvent(events, new AttachmentUpdateEvent());
    }
}
