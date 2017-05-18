package org.cru.godtools.base.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

public final class FileUtils {
    @Nullable
    public static File getFile(@NonNull final Context context, @Nullable final String name) {
        if (name != null) {
            final File dir = new File(context.getFilesDir(), "resources");
            // TODO: move directory creation logic to DownloadManager
            if ((dir.exists() || dir.mkdirs()) && dir.isDirectory()) {
                return new File(dir, name);
            }
        }
        return null;
    }
}
