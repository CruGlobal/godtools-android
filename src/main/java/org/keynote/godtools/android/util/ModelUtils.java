package org.keynote.godtools.android.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import org.keynote.godtools.android.model.Attachment;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;

public final class ModelUtils {
    @NonNull
    public static String getTranslationName(@Nullable final Translation translation,
                                            @Nullable final Tool tool) {
        return getTranslationName(translation != null ? translation.getName() : null,
                                  tool != null ? tool.getName() : null);
    }

    @NonNull
    public static String getTranslationName(@Nullable final String translationName, @Nullable final String toolName) {
        return MoreObjects.firstNonNull(translationName, Strings.nullToEmpty(toolName));
    }

    @Nullable
    public static String getAttachmentLocalFileName(@Nullable final Attachment attachment) {
        return attachment != null ? getAttachmentLocalFileName(attachment.getSha256(), attachment.getFileName()) :
                getAttachmentLocalFileName(null, null);
    }

    @Nullable
    public static String getAttachmentLocalFileName(@Nullable final String sha256, @Nullable final String fileName) {
        if (sha256 != null) {
            final int extensionIndex = fileName != null ? fileName.lastIndexOf('.') : -1;
            final String extension = extensionIndex != -1 ? fileName.substring(extensionIndex) : ".bin";
            return sha256 + extension;
        }
        return null;
    }
}
