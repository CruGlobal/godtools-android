package org.cru.godtools.xml.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;

import com.annimon.stream.Stream;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.support.v4.util.WeakLruCache;
import org.cru.godtools.base.util.FileUtils;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.xmlpull.v1.XmlPullParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.LruCache;
import timber.log.Timber;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public class ManifestManager {
    private static final String TAG = "ManifestManager";

    private static final int PARSING_CONCURRENCY = 6;

    private final LruCache<String, ListenableFuture<Manifest>> mCache = new WeakLruCache<>(6);

    @NonNull
    private final Context mContext;
    @NonNull
    private final GodToolsDao mDao;
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
        mContext = context;
        mDao = GodToolsDao.getInstance(mContext);
        mExecutor = new ThreadPoolExecutor(0, PARSING_CONCURRENCY, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                                           new NamedThreadFactory(ManifestManager.class.getSimpleName()));
    }

    @NonNull
    @AnyThread
    public ListenableFuture<Manifest> getLatestPublishedManifest(@NonNull final String toolCode,
                                                                 @NonNull final Locale locale) {
        final SettableFuture<Translation> latestTranslation = SettableFuture.create();
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> latestTranslation.set(
                mDao.streamCompat(
                        Query.select(Translation.class)
                                .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(toolCode, locale)
                                               .and(TranslationTable.SQL_WHERE_PUBLISHED)
                                               .and(TranslationTable.SQL_WHERE_DOWNLOADED))
                                .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC)
                                .limit(1))
                        .findFirst()
                        // update the last accessed time
                        .executeIfPresent(t -> {
                            t.setLastAccessed();
                            mDao.update(t, TranslationTable.COLUMN_LAST_ACCESSED);
                        })
                        .orElse(null)));

        return Futures.transformAsync(latestTranslation, this::getManifest, directExecutor());
    }

    @NonNull
    @AnyThread
    public ListenableFuture<Manifest> getManifest(@Nullable final Translation translation) {
        // short-circuit if we don't have a downloaded translation
        if (translation == null || !translation.isDownloaded()) {
            return Futures.immediateFuture(null);
        }

        // short-circuit if there isn't a manifest file name
        final String manifestName = translation.getManifestFileName();
        if (manifestName == null) {
            return Futures.immediateFuture(null);
        }

        // return the actual manifest
        return getManifest(manifestName, translation.getToolCode(), translation.getLanguageCode(), false);
    }

    @NonNull
    @AnyThread
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
                // find the file on disk
                final File file = FileUtils.getGodToolsFile(mContext, manifestName);

                // parse the Manifest from the specified XML file
                final Manifest manifest;
                final Closer closer = Closer.create();
                try {
                    final InputStream in = closer.register(new BufferedInputStream(new FileInputStream(file)));
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(in, "UTF-8");
                    parser.nextTag();
                    manifest = Manifest.fromXml(parser, manifestName, toolCode, locale);
                } catch (final FileNotFoundException e) {
                    brokenManifest(manifestName);
                    throw closer.rethrow(e);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }

                // complete this task
                manifestTask.set(manifest);
            } catch (final Throwable t) {
                Timber.tag(TAG)
                        .e(t, "Error loading manifest xml for %s (%s)", toolCode, LocaleCompat.toLanguageTag(locale));
                manifestTask.setException(t);
            }
        });

        // post-process the manifest
        final ListenableFuture<Manifest> finalManifestTask = Futures.transformAsync(manifestTask, manifest -> {
            // short-circuit if we don't have a manifest
            if (manifest == null) {
                return Futures.immediateFuture(null);
            }

            // parse all the pages
            final ListenableFuture<?> allPagesTask =
                    Futures.allAsList(Stream.of(manifest.getPages()).map(this::parsePage).toList());

            // return the manifest after all the pages have been parsed
            return Futures.transform(allPagesTask, i -> manifest, directExecutor());
        }, directExecutor());
        Futures.addCallback(finalManifestTask, new ManifestLoadingErrorCallback(toolCode, locale), directExecutor());
        return finalManifestTask;
    }

    @NonNull
    @AnyThread
    private ListenableFuture<Page> parsePage(@NonNull final Page page) {
        // parse the page
        final SettableFuture<Page> result = SettableFuture.create();
        mExecutor.execute(() -> {
            try {
                // find the file on disk
                final File file = FileUtils.getGodToolsFile(mContext, page.getLocalFileName());
                if (file == null) {
                    result.set(page);
                    return;
                }

                // parse the page XML into the page object
                final Closer closer = Closer.create();
                try {
                    final InputStream in = closer.register(new BufferedInputStream(new FileInputStream(file)));
                    final XmlPullParser parser = Xml.newPullParser();
                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                    parser.setInput(in, "UTF-8");
                    parser.nextTag();
                    page.parsePageXml(parser);
                } catch (final FileNotFoundException e) {
                    brokenManifest(page.getManifest().getManifestName());
                    throw closer.rethrow(e);
                } catch (final Throwable t) {
                    throw closer.rethrow(t);
                } finally {
                    closer.close();
                }

                // complete this task
                result.set(page);
            } catch (final Throwable t) {
                final Manifest manifest = page.getManifest();
                Timber.tag(TAG)
                        .e(t, "Error parsing page xml %s-%d (%s)", manifest.getCode(), page.getPosition(),
                           LocaleCompat.toLanguageTag(manifest.getLocale()));
                result.setException(t);
            }
        });
        return result;
    }

    @WorkerThread
    private void brokenManifest(@NonNull final String manifestName) {
        mDao.streamCompat(Query.select(Translation.class).where(TranslationTable.FIELD_MANIFEST.eq(manifestName)))
                .peek(t -> t.setDownloaded(false))
                .forEach(t -> mDao.update(t, TranslationTable.COLUMN_DOWNLOADED));
        EventBus.getDefault().post(TranslationUpdateEvent.INSTANCE);

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
