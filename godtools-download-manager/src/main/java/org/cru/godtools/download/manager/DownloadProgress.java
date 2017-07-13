package org.cru.godtools.download.manager;

import java.util.Arrays;

public final class DownloadProgress {
    private static final int INDETERMINATE_VAL = 0;
    public static final DownloadProgress INDETERMINATE = new DownloadProgress(INDETERMINATE_VAL, INDETERMINATE_VAL);

    private final int mProgress;
    private final int mMax;

    DownloadProgress(final int progress, final int max) {
        mMax = max <= 0 ? INDETERMINATE_VAL : max;
        mProgress =
                mMax == INDETERMINATE_VAL ? INDETERMINATE_VAL : progress < 0 ? 0 : progress > mMax ? mMax : progress;
    }

    DownloadProgress(final long progress, final long max) {
        this((int) progress, (int) max);
    }

    public boolean isIndeterminate() {
        return mMax == INDETERMINATE_VAL;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getMax() {
        return mMax;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DownloadProgress that = (DownloadProgress) o;
        return mProgress == that.mProgress && mMax == that.mMax;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new int[] {mProgress, mMax});
    }
}
