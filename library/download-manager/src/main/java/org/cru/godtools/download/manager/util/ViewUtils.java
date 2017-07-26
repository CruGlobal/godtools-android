package org.cru.godtools.download.manager.util;

import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import org.cru.godtools.download.manager.DownloadProgress;

public final class ViewUtils {
    public static void bindDownloadProgress(@Nullable final ProgressBar progressBar,
                                            @Nullable final DownloadProgress progress) {
        if (progressBar != null) {
            // update visibility
            final int visibility = progress != null ? View.VISIBLE : View.GONE;
            boolean animate = visibility == progressBar.getVisibility();
            progressBar.setVisibility(visibility);

            // update progress
            if (progress != null) {
                progressBar.setIndeterminate(progress.isIndeterminate());
                progressBar.setMax(progress.getMax());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(progress.getProgress(), animate);
                } else {
                    progressBar.setProgress(progress.getProgress());
                }
            }
        }
    }
}
