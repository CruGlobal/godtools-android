package org.keynote.godtools.android.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.android.util.FileUtils;

import java.io.File;

public final class LocalFile {
    public static final String INVALID_FILE_NAME = null;

    @Nullable
    private String mFileName = INVALID_FILE_NAME;

    @Nullable
    public String getFileName() {
        return mFileName;
    }

    public void setFileName(@Nullable final String name) {
        mFileName = name;
    }

    @Nullable
    public File getFile(@NonNull final Context context) {
        return FileUtils.getFile(context, mFileName);
    }
}
