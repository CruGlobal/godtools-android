package org.cru.godtools.articles.aem.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

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
    public static final class Key {
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

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Key key = (Key) o;
            return version == key.version &&
                    tool.equals(key.tool) &&
                    language.equals(key.language);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(tool, language, version);
        }
    }

    @Immutable
    @Entity(tableName = "translationAemImports", primaryKeys = {"tool", "language", "version", "aemImportUri"},
            foreignKeys = {
                    @ForeignKey(entity = TranslationRef.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"tool", "language", "version"},
                            childColumns = {"tool", "language", "version"}),
                    @ForeignKey(entity = AemImport.class,
                            onUpdate = ForeignKey.RESTRICT, onDelete = ForeignKey.CASCADE,
                            parentColumns = {"uri"}, childColumns = {"aemImportUri"}),
            },
            indices = {
                    @Index({"aemImportUri"})
            })
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
