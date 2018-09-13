package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

@Entity(tableName = "translations")
public class TranslationRef {
    @NonNull
    @Embedded
    @PrimaryKey
    public final Key key;

    public boolean processed = false;

    public TranslationRef(@NonNull final Key key) {
        this.key = key;
    }

    @Immutable
    public static class Key {
        @NonNull
        public final String tool;
        @NonNull
        public final Locale language;
        public final int version;

        public Key(@NonNull final String tool, @NonNull final Locale language, final int version) {
            this.tool = tool;
            this.language = language;
            this.version = version;
        }

        @Nullable
        @SuppressWarnings("ConstantConditions")
        public static Key from(@Nullable final Translation translation) {
            if (translation == null) {
                return null;
            }

            // short-circuit if the translation isn't valid
            final String tool = translation.getToolCode();
            final Locale language = translation.getLanguageCode();
            if (tool == null || tool.equals(Tool.INVALID_CODE) || language.equals(Language.INVALID_CODE)) {
                return null;
            }

            return new Key(tool, language, translation.getVersion());
        }
    }

    @Immutable
    @Entity(tableName = "translationAemImports", primaryKeys = {"tool", "language", "version", "aemImportUri"})
    public static class TranslationAemImport {
        @NonNull
        @Embedded
        public final Key translation;

        @NonNull
        public final Uri aemImportUri;

        public TranslationAemImport(@NonNull final Key translation, @NonNull final AemImport aemImport) {
            this(translation, aemImport.uri);
        }

        public TranslationAemImport(@NonNull final Key translation, @NonNull final Uri aemImportUri) {
            this.translation = translation;
            this.aemImportUri = aemImportUri;
        }
    }
}
