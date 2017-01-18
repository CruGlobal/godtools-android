package org.keynote.godtools.renderer.crureader.bo.GPage.Util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public final class FileUtils {
    public static boolean isSymLink(@NonNull final File file) throws IOException {
        final File canon;
        if (file.getParent() == null) {
            canon = file;
        } else {
            canon = new File(file.getParentFile().getCanonicalFile(), file.getName());
        }
        return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
    }

    public static boolean deleteRecursive(@NonNull final File f, final boolean followSymLinks) {
        try {
            if (f.isDirectory() && (followSymLinks || !isSymLink(f))) {
                for (final File c : f.listFiles()) {
                    deleteRecursive(c, followSymLinks);
                }
            }
        } catch (final IOException e) {
            // suppress exception
        }

        return f.delete();
    }

    @NonNull
    public static File getResourcesDir() {
        // prefer using external storage when available
        Context context = RenderSingleton.getInstance().getContext();
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            final File root = context.getExternalFilesDir(null);
            if (root != null) {
                final File dir = new File(root, "resources");
                //TODO: IOC crashlytics
                //Crashlytics.log("Potential External Resources Dir: " + dir);

                // make sure the resources directory exists before returning
                if (dir.isDirectory() || dir.mkdirs()) {
                    return dir;
                }
                //TODO: IOC crashlytics
                // log that we were unable to create external resources directory for any future exception/crash
                //Crashlytics.log("unable to create external resources directory");
            }
        }

        // fallback to internal storage
        final File dir = new File(context.getFilesDir(), "resources");
        if (!dir.isDirectory() && !dir.mkdirs()) {
            // we can't create an internal resources directory, log an error because something crazy may happen!
            //TODO: IOC crashlytics
            //Crashlytics.log("unable to create internal resources directory: " + dir);
        }
        return dir;
    }

    @Nullable
    public static File getTmpDir(@NonNull final Context context) {
        for (final File root : new File[] {context.getExternalFilesDir("tmp"),
                new File(context.getFilesDir(), "tmp")}) {
            // make sure the root directory exists before we proceed
            if (root == null || (!root.isDirectory() && !root.mkdirs())) {
                continue;
            }

            // create a temporary directory within root (attempt 3 times before giving up)
            for (int i = 0; i < 3; i++) {
                final File tmpDir = new File(root, UUID.randomUUID().toString());
                if (!tmpDir.exists() && tmpDir.mkdirs()) {
                    return tmpDir;
                }
            }
        }

        return null;
    }
}
