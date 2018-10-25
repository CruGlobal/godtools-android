package org.cru.godtools.model;

import android.content.Context;

import org.cru.godtools.base.util.FileUtils;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
