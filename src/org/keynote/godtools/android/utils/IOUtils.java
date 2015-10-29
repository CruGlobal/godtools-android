package org.keynote.godtools.android.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public final class IOUtils {
    // 640K should be enough for anybody -Bill Gates
    // (Bill Gates never actually said this)
    private static final int DEFAULT_BUFFER_SIZE = 640 * 1024;
    private static final int EOF = -1;

    public static void closeQuietly(@Nullable final HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }

    public static long copy(@NonNull final InputStream in, @NonNull final OutputStream out) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (EOF != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
