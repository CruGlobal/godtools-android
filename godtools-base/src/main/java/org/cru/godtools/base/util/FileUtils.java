package org.cru.godtools.base.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.File;

public final class FileUtils {
    @WorkerThread
    public static boolean createResourcesDir(@NonNull final Context context) {
        final File dir = getResourcesDir(context);
        return (dir.exists() || dir.mkdirs()) && dir.isDirectory();
    }

    @NonNull
    public static File getResourcesDir(@NonNull final Context context) {
        return new File(context.getFilesDir(), "resources");
    }

    @Nullable
    public static File getFile(@NonNull final Context context, @Nullable final String name) {
        return name != null ? new File(getResourcesDir(context), name) : null;
    }
}
