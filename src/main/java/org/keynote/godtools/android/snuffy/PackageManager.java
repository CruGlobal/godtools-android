package org.keynote.godtools.android.snuffy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.LruCache;
import android.util.Xml;

import com.crashlytics.android.Crashlytics;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.snuffy.model.GtManifest;
import org.keynote.godtools.android.snuffy.model.GtPage;
import org.keynote.godtools.android.utils.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PackageManager {
    private static PackageManager INSTANCE;

    @NonNull
    private final Context mContext;
    private final ExecutorService mExecutor;

    private final LruCache<String, ListenableFuture<GtManifest>> mCache = new LruCache<>(6);

    private PackageManager(@NonNull final Context context) {
        mContext = context;
        mExecutor = Executors.newFixedThreadPool(3, new NamedThreadFactory("PackageManager"));
        if (mExecutor instanceof ThreadPoolExecutor) {
            ((ThreadPoolExecutor) mExecutor).setKeepAliveTime(30, TimeUnit.SECONDS);
            ((ThreadPoolExecutor) mExecutor).allowCoreThreadTimeOut(true);
        }
    }

    public static synchronized PackageManager getInstance(@NonNull final Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PackageManager(context.getApplicationContext());
        }

        return INSTANCE;
    }

    @NonNull
    public ListenableFuture<GtManifest> getManifest(@NonNull final GTPackage gtPackage, final boolean forceReload) {
        return getManifest(gtPackage.getConfigFileName(), gtPackage.getCode(), gtPackage.getLanguage(), forceReload);
    }

    @NonNull
    @Deprecated
    public ListenableFuture<GtManifest> getManifest(@NonNull final String manifestFileName, @NonNull final String code,
                                                    @NonNull final String language, final boolean forceReload) {
        final SettableFuture<GtManifest> resp;
        synchronized (mCache) {
            ListenableFuture<GtManifest> cached = mCache.get(manifestFileName);

            // make sure we have a valid value cached
            // XXX: should we invalidate an entry after load failure instead?
            if (cached != null && cached.isDone()) {
                try {
                    if (cached.get() == null) {
                        mCache.remove(manifestFileName);
                        cached = null;
                    }
                } catch (final InterruptedException e) {
                    // propagate interrupt
                    Thread.currentThread().interrupt();
                } catch (final ExecutionException e) {
                    // error loading the manifest, remove it from the cache
                    mCache.remove(manifestFileName);
                    cached = null;
                }
            }

            // short-circuit if we have a valid future and aren't forcing a reload to prevent duplicate loads
            if (cached != null && !forceReload) {
                return cached;
            }

            // create a new future for this request (and cache it before starting to process)
            resp = SettableFuture.create();
            mCache.put(manifestFileName, resp);
        }

        // trigger load on our background pool thread
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final GtManifest manifest = loadManifest(manifestFileName, code, language);
                    loadPages(manifest);
                    resp.set(manifest);
                } catch (final Throwable t) {
                    resp.setException(t);
                }
            }
        });

        // return the response
        return resp;
    }

    @NonNull
    @WorkerThread
    GtManifest loadManifest(@NonNull final String manifestFileName, @NonNull final String code,
                            @NonNull final String language) throws IOException, XmlPullParserException {
        final Closer closer = Closer.create();
        try {
            // open file
            final File file = new File(FileUtils.getResourcesDir(mContext), manifestFileName);
            final InputStream fileIn = closer.register(new FileInputStream(file));
            final InputStream bufIn = closer.register(new BufferedInputStream(fileIn));

            try {
                // initialize pull parser
                final XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(bufIn, "UTF-8");
                parser.nextTag();

                // parse & return the package manifest
                return GtManifest.fromXml(parser, code, language);
            } catch (final Throwable t) {
                Crashlytics.log("error processing main package manifest: " + file.toString());
                Crashlytics.logException(t);
                throw t;
            }
        } catch (final Throwable t) {
            throw closer.rethrow(t, XmlPullParserException.class);
        } finally {
            closer.close();
        }
    }

    @WorkerThread
    void loadPages(@NonNull final GtManifest manifest) throws IOException, XmlPullParserException {
        loadPage(manifest.getAbout());
        for (final GtPage page : manifest.getPages()) {
            loadPage(page);
        }
    }

    @WorkerThread
    private void loadPage(@NonNull final GtPage page) throws IOException, XmlPullParserException {
        final Closer closer = Closer.create();
        try {
            // open file
            final File file = new File(FileUtils.getResourcesDir(mContext), page.getFileName());
            final InputStream fileIn = closer.register(new FileInputStream(file));
            final InputStream bufIn = closer.register(new BufferedInputStream(fileIn));

            try {
                // initialize pull parser
                final XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                parser.setInput(bufIn, "UTF-8");
                parser.nextTag();

                // parse & return the package manifest
                page.parsePageXml(parser);
            } catch (final Throwable t) {
                Crashlytics.log("error processing page xml: " + file.toString());
                Crashlytics.logException(t);
                throw t;
            }
        } catch (final Throwable t) {
            throw closer.rethrow(t, XmlPullParserException.class);
        } finally {
            closer.close();
        }
    }
}
