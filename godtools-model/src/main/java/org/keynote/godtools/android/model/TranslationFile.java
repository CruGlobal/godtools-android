package org.keynote.godtools.android.model;

import android.support.annotation.Nullable;

import org.cru.godtools.model.LocalFile;

public final class TranslationFile {
    @Nullable
    private Long mTranslationId = Translation.INVALID_ID;
    @Nullable
    private Translation mTranslation;
    @Nullable
    private String mFileName = LocalFile.INVALID_FILE_NAME;
    @Nullable
    private LocalFile mFile;

    public long getTranslationId() {
        return mTranslationId != null && mTranslationId != Translation.INVALID_ID ? mTranslationId :
                mTranslation != null ? mTranslation.getId() : Translation.INVALID_ID;
    }

    public void setTranslationId(@Nullable final Long id) {
        mTranslationId = id;
    }

    public void setTranslation(@Nullable final Translation translation) {
        mTranslation = translation;
    }

    @Nullable
    public String getFileName() {
        return mFileName != null && !mFileName.equals(LocalFile.INVALID_FILE_NAME) ? mFileName :
                mFile != null ? mFile.getFileName() : LocalFile.INVALID_FILE_NAME;
    }

    public void setFileName(@Nullable final String name) {
        mFileName = name;
    }

    public void setFile(@Nullable final LocalFile file) {
        mFile = file;
    }
}
