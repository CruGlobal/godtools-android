package org.keynote.godtools.android.util;

import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.base.util.FileUtils;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.sync.service.GodToolsDownloadManager.DownloadProgress;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.model.Tool;

public final class ViewUtils {
    public static void bindShares(@Nullable final TextView view, @Nullable final Tool tool) {
        bindShares(view, tool != null ? tool.getTotalShares() : 0);
    }

    public static void bindShares(@Nullable final TextView view, final int shares) {
        if (view != null) {
            view.setText(view.getResources().getQuantityString(R.plurals.label_tools_shares, shares, shares));
        }
    }

    public static void bindLocalImage(@Nullable final PicassoImageView view, @Nullable final Attachment attachment) {
        bindLocalImage(view, attachment != null && attachment.isDownloaded() ? attachment.getLocalFileName() : null);
    }

    public static void bindLocalImage(@Nullable final PicassoImageView view, @Nullable final String filename) {
        if (view != null) {
            view.setPicassoFile(FileUtils.getFile(view.getContext(), filename));
        }
    }

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
