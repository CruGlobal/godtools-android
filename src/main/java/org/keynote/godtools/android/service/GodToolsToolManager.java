package org.keynote.godtools.android.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;

import com.annimon.stream.ComparatorCompat;
import com.annimon.stream.Stream;
import com.annimon.stream.function.ToLongFunction;
import com.google.common.base.Objects;
import com.google.common.io.Closer;
import com.google.common.io.CountingInputStream;
import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.task.EventBusDelayedPost;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.IOUtils.ProgressCallback;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.Settings;
import org.keynote.godtools.android.api.GodToolsApi;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.event.LanguageUpdateEvent;
import org.keynote.godtools.android.event.ToolUpdateEvent;
import org.keynote.godtools.android.event.TranslationUpdateEvent;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.LocalFile;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.model.Translation;
import org.keynote.godtools.android.model.TranslationFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.ccci.gto.android.common.util.ThreadUtils.getLock;

public final class GodToolsToolManager {
    private static final int DOWNLOAD_CONCURRENCY = 2;

    private static final ArrayMap<String, Object> LOCKS_FILES = new ArrayMap<>();
    private static final ArrayMap<TranslationKey, Object> LOCKS_TRANSLATION_DOWNLOADS = new ArrayMap<>();

    private final Context mContext;
    private final GodToolsApi mApi;
    private final GodToolsDao mDao;
    private final EventBus mEventBus;
    private final Settings mPrefs;
    private final ThreadPoolExecutor mExecutor;
    private final BlockingQueue<Runnable> mExecutorQueue;

    @NonNull
    PriorityQueue<TranslationKey> mPending;
    private final Object mPendingLock = new Object();

    private GodToolsToolManager(@NonNull final Context context) {
        mContext = context;
        mApi = GodToolsApi.getInstance(mContext);
        mDao = GodToolsDao.getInstance(mContext);
        mEventBus = EventBus.getDefault();
        mPrefs = Settings.getInstance(mContext);
        mPending = new PriorityQueue<>(11, TranslationKey.COMPARATOR);

        // build download executor
        mExecutorQueue = new LinkedBlockingQueue<>(Math.max(DOWNLOAD_CONCURRENCY / 2, 1));
        mExecutor = new ThreadPoolExecutor(0, DOWNLOAD_CONCURRENCY, 10, TimeUnit.SECONDS, mExecutorQueue,
                                           new NamedThreadFactory(GodToolsToolManager.class.getSimpleName()));

        // register with EventBus
        mEventBus.register(this);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsToolManager sInstance;
    @NonNull
    public static GodToolsToolManager getInstance(@NonNull final Context context) {
        synchronized (GodToolsToolManager.class) {
            if (sInstance == null) {
                sInstance = new GodToolsToolManager(context.getApplicationContext());
            }
        }

        return sInstance;
    }

    /* BEGIN lifecycle */

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLanguageUpdate(@NonNull final LanguageUpdateEvent event) {
        enqueuePendingPublishedTranslations();
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onToolUpdate(@NonNull final ToolUpdateEvent event) {
        enqueuePendingPublishedTranslations();
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        enqueuePendingPublishedTranslations();
    }

    /* END lifecycle */

    public void addLanguage(@Nullable final Locale locale) {
        if (locale != null) {
            final Language language = new Language();
            language.setCode(locale);
            language.setAdded(true);
            final ListenableFuture<Integer> update = mDao.updateAsync(language, LanguageTable.COLUMN_ADDED);
            update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new LanguageUpdateEvent()),
                               directExecutor());
        }
    }

    public void removeLanguage(@Nullable final Locale locale) {
        if (locale != null && !mPrefs.isLanguageProtected(locale)) {
            // clear the parallel language if it is the language being removed
            if (locale.equals(mPrefs.getParallelLanguage())) {
                mPrefs.setParallelLanguage(null);
            }

            // remove the language from the device
            final Language language = new Language();
            language.setCode(locale);
            language.setAdded(false);
            final ListenableFuture<Integer> update = mDao.updateAsync(language, LanguageTable.COLUMN_ADDED);
            update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new LanguageUpdateEvent()),
                               directExecutor());
        }
    }

    public void addTool(final long id) {
        final Tool tool = new Tool();
        tool.setId(id);
        tool.setAdded(true);
        final ListenableFuture<Integer> update = mDao.updateAsync(tool, ToolTable.COLUMN_ADDED);
        update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new ToolUpdateEvent()), directExecutor());
    }

    public void removeTool(final long id) {
        final Tool tool = new Tool();
        tool.setId(id);
        tool.setAdded(false);
        final ListenableFuture<Integer> update = mDao.updateAsync(tool, ToolTable.COLUMN_ADDED);
        update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new ToolUpdateEvent()), directExecutor());
    }

    @WorkerThread
    void downloadLatestPublishedTranslation(@NonNull final TranslationKey key) {
        // lock translation
        synchronized (getLock(LOCKS_TRANSLATION_DOWNLOADS, key)) {
            // process the most recent published version
            final Query<Translation> query = Query.select(Translation.class)
                    .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(key.mToolId, key.mLocale)
                                   .and(TranslationTable.SQL_WHERE_PUBLISHED))
                    .orderBy(TranslationTable.COLUMN_VERSION + " DESC")
                    .limit(1);
            final Translation translation = mDao.streamCompat(query).findFirst().orElse(null);

            // only process this translation if it's not already downloaded
            if (translation != null && !translation.isDownloaded()) {
                try {
                    final Response<ResponseBody> response = mApi.translations.download(translation.getId()).execute();
                    if (response.isSuccessful()) {
                        final ResponseBody body = response.body();
                        if (body != null) {
                            processZipDownload(translation, body);

                            // mark translation as downloaded
                            translation.setDownloaded(true);
                            mDao.update(translation, TranslationTable.COLUMN_DOWNLOADED);
                            mEventBus.post(new TranslationUpdateEvent());
                        }
                    }
                } catch (final IOException ignored) {
                }
            }
        }
    }

    /**
     * Process a streaming zip response.
     */
    private void processZipDownload(@NonNull final Translation translation, @NonNull final ResponseBody body)
            throws IOException {
        final long size = body.contentLength();
        final Closer closer = Closer.create();
        try {
            final CountingInputStream count = closer.register(new CountingInputStream(body.byteStream()));
            final ZipInputStream zin = closer.register(new ZipInputStream(new BufferedInputStream(count)));
            final ProgressCallback progressCallback = (s) -> updateProgress(translation, count.getCount(), size);

            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                final String fileName = ze.getName();
                synchronized (getLock(LOCKS_FILES, fileName)) {
                    // write the file if it hasn't been downloaded before
                    LocalFile localFile = mDao.find(LocalFile.class, fileName);
                    if (localFile == null) {
                        // create a new local file object
                        localFile = new LocalFile();
                        localFile.setFileName(fileName);

                        // short-circuit if the local file doesn't exist
                        final File file = localFile.getFile(mContext);
                        if (file == null) {
                            throw new FileNotFoundException(fileName + " (File could not be created)");
                        }

                        // write file
                        final OutputStream os = closer.register(new FileOutputStream(file));
                        IOUtils.copy(zin, os, progressCallback);
                        os.flush();
                        os.close();

                        // store local file in database
                        mDao.updateOrInsert(localFile);
                    }

                    // associate this file with this translation
                    final TranslationFile translationFile = new TranslationFile();
                    translationFile.setTranslation(translation);
                    translationFile.setFile(localFile);
                    mDao.updateOrInsert(translationFile);
                }

                updateProgress(translation, count.getCount(), size);
            }
        } catch (final Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private void updateProgress(@NonNull final Translation translation, final long progress, final long total) {
        // TODO
    }

    @WorkerThread
    private void enqueuePendingPublishedTranslations() {
        mDao.streamCompat(Query.select(Translation.class)
                                  .joins(TranslationTable.SQL_JOIN_LANGUAGE, TranslationTable.SQL_JOIN_TOOL)
                                  .where(LanguageTable.FIELD_ADDED.eq(true)
                                                 .and(ToolTable.FIELD_ADDED.eq(true))
                                                 .and(TranslationTable.SQL_WHERE_PUBLISHED)
                                                 .and(TranslationTable.FIELD_DOWNLOADED.eq(false))))
                .map(TranslationKey::new)
                .peek(this::updateLocaleType)
                .forEach(this::enqueue);

        scheduleWork();
    }

    @WorkerThread
    void scheduleWork() {
        synchronized (mPendingLock) {
            while (!mPending.isEmpty() && mExecutorQueue.remainingCapacity() > 0) {
                final TranslationKey key = mPending.poll();
                if (key != null) {
                    mExecutor.execute(new DownloadTranslationRunnable(key));
                }
            }
        }
    }

    @WorkerThread
    void enqueue(@NonNull final TranslationKey key) {
        synchronized (mPendingLock) {
            mPending.add(key);
        }
    }

    // TODO: run when primary or parallel language changes
    @WorkerThread
    private void resortQueue() {
        synchronized (mPendingLock) {
            // short-circuit if the pending queue is empty
            if (mPending.isEmpty()) {
                return;
            }

            // rebuild the queue with updated locale types
            final PriorityQueue<TranslationKey> old = mPending;
            mPending = new PriorityQueue<>(old.size(), TranslationKey.COMPARATOR);
            Stream.of(old).distinct()
                    .peek(this::updateLocaleType)
                    .forEach(mPending::offer);
        }
    }

    private void updateLocaleType(@NonNull final TranslationKey key) {
        key.updateLocaleType(mPrefs.getPrimaryLanguage(), mPrefs.getParallelLanguage());
    }

    static final class TranslationKey {
        private static final int TYPE_PRIMARY = 1;
        private static final int TYPE_PARALLEL = 2;
        private static final int TYPE_OTHER = 3;

        static final Comparator<TranslationKey> COMPARATOR =
                ComparatorCompat.<TranslationKey>comparingInt(k -> k.mLocaleType)
                        .thenComparingLong((ToLongFunction<? super TranslationKey>) k -> k.mToolId);

        final long mToolId;
        @NonNull
        final Locale mLocale;
        transient int mLocaleType = TYPE_OTHER;

        TranslationKey(@NonNull final Translation translation) {
            this(translation.getToolId(), translation.getLanguageCode());
        }

        TranslationKey(final long toolId, @NonNull final Locale locale) {
            mToolId = toolId;
            mLocale = locale;
        }

        void updateLocaleType(@NonNull final Locale primary, @Nullable final Locale parallel) {
            mLocaleType = mLocale.equals(primary) ? TYPE_PRIMARY :
                    mLocale.equals(parallel) ? TYPE_PARALLEL : TYPE_OTHER;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TranslationKey that = (TranslationKey) o;
            return mToolId == that.mToolId &&
                    Objects.equal(mLocale, that.mLocale);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(mToolId, mLocale);
        }
    }

    final class DownloadTranslationRunnable implements Runnable {
        @NonNull
        final TranslationKey mKey;

        DownloadTranslationRunnable(@NonNull final TranslationKey key) {
            mKey = key;
        }

        @Override
        public void run() {
            downloadLatestPublishedTranslation(mKey);
            scheduleWork();
        }
    }
}
