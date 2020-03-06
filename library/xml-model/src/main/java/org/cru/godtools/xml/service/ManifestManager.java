package org.cru.godtools.xml.service;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.support.v4.util.WeakLruCache;
import org.cru.godtools.model.Translation;
import org.cru.godtools.xml.model.Manifest;
import org.keynote.godtools.android.db.Contract.TranslationTable;

import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;
import timber.log.Timber;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public class ManifestManager extends KotlinManifestManager {
    private static final String TAG = "ManifestManager";

    private static final int PARSING_CONCURRENCY = 6;

    private final LruCache<String, ListenableFuture<Manifest>> mCache = new WeakLruCache<>(6);

    private final ThreadPoolExecutor mExecutor;

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static ManifestManager sInstance;

    @NonNull
    public static synchronized ManifestManager getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new ManifestManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private ManifestManager(@NonNull final Context context) {
        super(context);
        mExecutor = new ThreadPoolExecutor(0, PARSING_CONCURRENCY, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                                           new NamedThreadFactory(ManifestManager.class.getSimpleName()));
    }

    @Nullable
    @WorkerThread
    public Manifest getLatestPublishedManifest(@NonNull final String toolCode, @NonNull final Locale locale) {
        final Translation translation = dao.getLatestTranslation(toolCode, locale, true, true).orElse(null);
        if (translation == null) {
            return null;
        }

        // update the last accessed time
        translation.updateLastAccessed();
        dao.update(translation, TranslationTable.COLUMN_LAST_ACCESSED);

        // return the manifest for this translation
        try {
            return getManifestBlocking(translation);
        } catch (InterruptedException e) {
            // set interrupted flag and return immediately
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @NonNull
    @MainThread
    public ListenableFuture<Manifest> getManifest(@NonNull final String manifestName, @Nullable final String toolCode,
                                                  @NonNull final Locale locale) {
        return getManifest(manifestName, toolCode, locale, false);
    }

    @NonNull
    @AnyThread
    private ListenableFuture<Manifest> getManifest(@NonNull final String manifestName, @Nullable final String toolCode,
                                                   @NonNull final Locale locale, final boolean forceReload) {
        synchronized (mCache) {
            if (!forceReload) {
                // check to see if this manifest is already loaded (or currently loading)
                final ListenableFuture<Manifest> cached = mCache.get(manifestName);
                if (cached != null) {
                    return cached;
                }
            }

            // short-circuit if we don't have a valid tool code
            if (toolCode == null) {
                return Futures.immediateFuture(null);
            }

            // trigger a background load of this manifest
            final ListenableFuture<Manifest> manifest = loadManifest(manifestName, toolCode, locale);
            mCache.put(manifestName, manifest);
            return manifest;
        }
    }

    @NonNull
    @AnyThread
    private ListenableFuture<Manifest> loadManifest(@NonNull final String manifestName, @NonNull final String toolCode,
                                                    @NonNull final Locale locale) {
        // load the manifest
        final SettableFuture<Manifest> manifestTask = SettableFuture.create();
        mExecutor.execute(() -> {
            try {
                final Result result = manifestParser.parseBlocking(manifestName, toolCode, locale);
                if (result instanceof Result.Data) {
                    manifestTask.set(((Result.Data) result).getManifest());
                } else if (result instanceof Result.Error.NotFound || result instanceof Result.Error.Corrupted) {
                    brokenManifest(manifestName);
                    manifestTask.set(null);
                } else {
                    manifestTask.set(null);
                }
            } catch (final Throwable t) {
                Timber.tag(TAG)
                        .e(t, "Error loading manifest xml for %s (%s)", toolCode, LocaleCompat.toLanguageTag(locale));
                manifestTask.setException(t);
            }
        });
        Futures.addCallback(manifestTask, new ManifestLoadingErrorCallback(toolCode, locale), directExecutor());
        return manifestTask;
    }

    @Override
    @WorkerThread
    protected void brokenManifest(@NonNull final String manifestName) {
        super.brokenManifest(manifestName);

        // remove the broken manifest from the cache
        synchronized (mCache) {
            mCache.remove(manifestName);
        }
    }

    static class ManifestLoadingErrorCallback implements FutureCallback<Manifest> {
        private final String mTool;
        private final Locale mLocale;

        ManifestLoadingErrorCallback(final String tool, final Locale locale) {
            mTool = tool;
            mLocale = locale;
        }

        @Override
        public void onSuccess(final Manifest result) {}

        @Override
        public void onFailure(final Throwable t) {
            Timber.tag(TAG)
                    .e(t, "Error loading manifest for %s (%s)", mTool, LocaleCompat.toLanguageTag(mLocale));
        }
    }
}
