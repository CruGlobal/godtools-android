package org.cru.godtools.article.aem.service;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.hash.HashCode;
import com.google.common.io.Closer;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.ThreadUtils;
import org.cru.godtools.article.aem.api.AemApi;
import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.db.ResourceDao;
import org.cru.godtools.article.aem.db.TranslationRepository;
import org.cru.godtools.article.aem.model.AemImport;
import org.cru.godtools.article.aem.model.Article;
import org.cru.godtools.article.aem.model.Resource;
import org.cru.godtools.article.aem.service.support.AemJsonParserKt;
import org.cru.godtools.article.aem.service.support.HtmlParserKt;
import org.cru.godtools.article.aem.util.ResourceUtilsKt;
import org.cru.godtools.article.aem.util.UriUtils;
import org.cru.godtools.base.tool.service.ManifestManager;
import org.cru.godtools.base.util.PriorityRunnable;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.xml.model.Manifest;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.room.InvalidationTracker;
import kotlin.sequences.SequencesKt;
import okhttp3.ResponseBody;
import retrofit2.Response;
import timber.log.Timber;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.synchronizedMap;
import static org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS;
import static org.cru.godtools.article.aem.model.Constants.TABLE_NAME_RESOURCE;

/**
 * This class hold all the logic for maintaining a local cache of AEM Articles.
 */
@Singleton
public class AemArticleManager {
    private static final String TAG = "AemArticleManager";

    private static final int MSG_CLEAN = 1;

    private static final int TASK_CONCURRENCY = 10;
    private static final long CACHE_BUSTING_INTERVAL_JSON = HOUR_IN_MS;
    private static final long CLEANER_INTERVAL_IN_MS = HOUR_IN_MS;

    private final ArticleRoomDatabase mAemDb;
    private final AemApi mApi;
    private final Context mContext;
    private final GodToolsDao mDao;
    private final ThreadPoolExecutor mExecutor;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final ManifestManager mManifestManager;

    // Task synchronization locks and flags
    private static final ReadWriteLock LOCK_FILESYSTEM = new ReentrantReadWriteLock();
    private final Object mExtractAemImportsLock = new Object();
    private final AtomicBoolean mExtractAemImportsQueued = new AtomicBoolean(false);
    final Map<Uri, Object> mSyncAemImportLocks = new HashMap<>();
    final Map<Uri, Object> mGenerateShareUriLocks = new HashMap<>();
    final Map<Uri, Object> mDownloadArticleLocks = new HashMap<>();
    final Map<Uri, Object> mDownloadResourceLocks = new HashMap<>();
    final Object mCleanOrphanedFilesLock = new Object();

    // Task de-dup related objects
    @Nullable
    private CleanOrphanedFilesTask mCleanOrphanedFilesTask;
    private final Map<Uri, SyncAemImportTask> mSyncAemImportTasks = synchronizedMap(new HashMap<>());
    private final Map<Uri, DownloadArticleTask> mDownloadArticleTasks = synchronizedMap(new HashMap<>());
    private final Map<Uri, DownloadResourceTask> mDownloadResourceTasks = synchronizedMap(new HashMap<>());

    @Inject
    AemArticleManager(@NonNull final Context context, final EventBus eventBus, final GodToolsDao dao, final AemApi api,
                      final ManifestManager manifestManager, final ArticleRoomDatabase aemDb) {
        mApi = api;
        mContext = context.getApplicationContext();
        mAemDb = aemDb;
        mAemDb.getInvalidationTracker().addObserver(new RoomDatabaseChangeTracker(TABLE_NAME_RESOURCE));
        mDao = dao;
        mExecutor = new ThreadPoolExecutor(0, TASK_CONCURRENCY, 10, TimeUnit.SECONDS,
                                           new PriorityBlockingQueue<>(11, PriorityRunnable.COMPARATOR),
                                           new NamedThreadFactory(AemArticleManager.class.getSimpleName()));
        mManifestManager = manifestManager;

        eventBus.register(this);

        // trigger some base sync tasks
        // TODO: maybe these sync tasks should be triggered elsewhere?
        enqueueExtractAemImportsFromManifests();
        enqueueSyncStaleAemImports();

        // perform an initial clean of any orphaned files
        enqueueCleanOrphanedFiles();
    }

    // region Lifecycle
    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        enqueueExtractAemImportsFromManifests();
    }
    // endregion Lifecycle

    // region Task Scheduling Methods

    @AnyThread
    private void enqueueExtractAemImportsFromManifests() {
        // only enqueue task if it's not currently enqueued
        if (!mExtractAemImportsQueued.getAndSet(true)) {
            mExecutor.execute(this::extractAemImportsFromManifestsTask);
        }
    }

    @AnyThread
    public ListenableFuture<?> enqueueSyncManifestAemImports(@Nullable final Manifest manifest, final boolean force) {
        if (manifest == null) {
            return Futures.immediateFuture(null);
        }

        return Futures.successfulAsList(
                Stream.of(manifest.getAemImports())
                        .map(uri -> enqueueSyncAemImport(uri, force))
                        .toList());
    }

    @AnyThread
    private void enqueueSyncStaleAemImports() {
        mExecutor.execute(this::syncStaleAemImportsTask);
    }

    @AnyThread
    private ListenableFuture<Boolean> enqueueSyncAemImport(@NonNull final Uri uri, final boolean force) {
        final SyncAemImportTask existing = mSyncAemImportTasks.get(uri);
        if (existing != null && existing.updateTask(force)) {
            return existing.mResult;
        }

        // create a new sync task
        final SyncAemImportTask task = new SyncAemImportTask(uri);
        task.updateTask(force);
        mSyncAemImportTasks.put(uri, task);
        mExecutor.execute(task);
        return task.mResult;
    }

    @NonNull
    @AnyThread
    public ListenableFuture<Boolean> downloadDeeplinkedArticle(@NonNull final Uri uri) {
        // create an AemImport for the deeplinked article
        final SettableFuture<Boolean> deeplinkTask = SettableFuture.create();
        mExecutor.execute(() -> {
            createAemImportForDeeplinkedArticleTask(uri);
            deeplinkTask.set(true);
        });

        final ListenableFuture<?> syncAemImportTask =
                Futures.transformAsync(deeplinkTask, t -> enqueueSyncAemImport(uri, false), directExecutor());
        return Futures.transformAsync(syncAemImportTask, t -> downloadArticle(uri, false), directExecutor());
    }

    @NonNull
    @AnyThread
    public ListenableFuture<Boolean> downloadArticle(@NonNull final Uri uri, final boolean force) {
        // try updating a task that is currently enqueued
        final DownloadArticleTask existing = mDownloadArticleTasks.get(uri);
        if (existing != null && existing.updateTask(force)) {
            return existing.mResult;
        }

        // create a new sync task
        final DownloadArticleTask task = new DownloadArticleTask(uri);
        task.updateTask(force);
        mDownloadArticleTasks.put(uri, task);
        mExecutor.execute(task);
        return task.mResult;
    }

    @AnyThread
    public ListenableFuture<Boolean> enqueueDownloadResource(@NonNull final Uri uri, final boolean force) {
        // try updating a task that is currently enqueued
        final DownloadResourceTask existing = mDownloadResourceTasks.get(uri);
        if (existing != null && existing.updateTask(force)) {
            return existing.mResult;
        }

        // create a new sync task
        final DownloadResourceTask task = new DownloadResourceTask(uri);
        task.updateTask(force);
        mDownloadResourceTasks.put(uri, task);
        mExecutor.execute(task);
        return task.mResult;
    }

    @AnyThread
    void scheduleCleanOrphanedFiles() {
        // remove any pending executions
        mHandler.removeMessages(MSG_CLEAN);

        // schedule another execution
        final Message m = Message.obtain(mHandler, this::enqueueCleanOrphanedFiles);
        m.what = MSG_CLEAN;
        mHandler.sendMessageDelayed(m, CLEANER_INTERVAL_IN_MS);
    }

    @AnyThread
    void enqueueCleanOrphanedFiles() {
        // try updating a task that is currently enqueued
        if (mCleanOrphanedFilesTask != null && !mCleanOrphanedFilesTask.mStarted) {
            return;
        }

        // otherwise create a new task
        mCleanOrphanedFilesTask = new CleanOrphanedFilesTask();
        mExecutor.execute(mCleanOrphanedFilesTask);
    }

    // endregion Task Scheduling Methods

    // region Tasks

    /**
     * This task is responsible for syncing the list of all AEM Import URLs defined in manifests to the locally cached
     * AEM Article database.
     */
    @WorkerThread
    void extractAemImportsFromManifestsTask() {
        synchronized (mExtractAemImportsLock) {
            mExtractAemImportsQueued.set(false);

            // load all downloaded translations of the "article" tool type
            final Query<Translation> query = Query.select(Translation.class)
                    .join(TranslationTable.SQL_JOIN_TOOL)
                    .where(ToolTable.FIELD_TYPE.eq(Tool.Type.ARTICLE).and(TranslationTable.SQL_WHERE_DOWNLOADED));
            final List<Translation> translations = mDao.get(query);

            final TranslationRepository repository = mAemDb.translationRepository();
            for (final Translation translation : translations) {
                // skip any translation we have already processed
                if (repository.isProcessed(translation)) {
                    continue;
                }

                try {
                    // add AEM imports extracted from the manifest to the AEM article cache
                    final Manifest manifest = mManifestManager.getManifestBlocking(translation);
                    if (manifest != null) {
                        repository.addAemImports(translation, manifest.getAemImports());
                        enqueueSyncManifestAemImports(manifest, false);
                    }
                } catch (InterruptedException e) {
                    // return immediately if interrupted
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // prune any translations that we no longer have downloaded.
            repository.removeMissingTranslations(translations);
        }
    }

    /**
     * This task is responsible for triggering syncs of any stale Aem Imports
     */
    @WorkerThread
    void syncStaleAemImportsTask() {
        Stream.of(mAemDb.aemImportDao().getAll())
                .filter(AemImport::isStale)
                .forEach(i -> enqueueSyncAemImport(i.getUri(), false));
    }

    /**
     * This task is responsible for syncing an individual AEM Import url to the AEM Article database.
     *
     * @param baseUri The base AEM Import URL to sync
     * @param force   This flag indicates that this sync should ignore the lastUpdated time
     */
    @WorkerThread
    @GuardedBy("mSyncAemImportLocks")
    void syncAemImportTask(@NonNull final Uri baseUri, final boolean force) {
        // short-circuit if this isn't an hierarchical absolute URL
        if (!(baseUri.isHierarchical() && baseUri.isAbsolute())) {
            return;
        }

        // short-circuit if there isn't an AemImport for the specified Uri
        final AemImport aemImport = mAemDb.aemImportDao().find(baseUri);
        if (aemImport == null) {
            return;
        }

        // short-circuit if the AEM Import isn't stale and we aren't forcing a sync
        if (!aemImport.isStale() && !force) {
            return;
        }

        // fetch the raw json
        JSONObject json = null;
        try {
            final long timestamp = force ? System.currentTimeMillis() :
                    roundTimestamp(System.currentTimeMillis(), CACHE_BUSTING_INTERVAL_JSON);
            json = mApi.getJson(UriUtils.addExtension(baseUri, "9999.json"), timestamp)
                    .execute()
                    .body();
        } catch (final IOException e) {
            Timber.tag("AEMDownloadManager")
                    .v(e, "Error downloading AEM json");
        }
        if (json == null) {
            return;
        }

        // parse & store articles
        final List<Article> articles = SequencesKt.toList(AemJsonParserKt.findAemArticles(json, baseUri));
        mAemDb.aemImportRepository().processAemImportSync(aemImport, articles);

        // enqueue a couple article specific tasks
        for (final Article article : articles) {
            downloadArticle(article.getUri(), false);
        }
    }

    @WorkerThread
    void createAemImportForDeeplinkedArticleTask(@NonNull final Uri uri) {
        // create & access an AemImport to trigger the download pipeline
        final AemImport aemImport = new AemImport(uri);
        aemImport.setLastAccessed(new Date());
        mAemDb.aemImportRepository().accessAemImport(aemImport);

        // enqueue a sync of the AemImport
        enqueueSyncAemImport(uri, false);
    }

    /**
     * This task will download the html content for a specific article.
     */
    @WorkerThread
    @GuardedBy("mDownloadArticleLocks")
    void downloadArticleTask(@NonNull final Uri uri, final boolean force) {
        // short-circuit if there isn't an Article for the specified Uri
        final Article article = mAemDb.articleDao().find(uri);
        if (article == null) {
            return;
        }

        // short-circuit if the Article isn't stale and we aren't forcing a download
        if (article.getUuid().equals(article.getContentUuid()) && !force) {
            return;
        }

        // download the article html
        try {
            final Response<String> response =
                    mApi.downloadArticle(UriUtils.addExtension(article.getUri(), "html")).execute();
            if (response.code() == HTTP_OK) {
                article.setContentUuid(article.getUuid());
                article.setContent(response.body());
                article.setResources(HtmlParserKt.extractResources(article));
                mAemDb.articleRepository().updateContent(article);

                downloadResourcesNeedingUpdate(mAemDb.resourceDao().getAllForArticle(article.getUri()));
            }
        } catch (final IOException e) {
            Timber.tag(TAG)
                    .d(e, "Error downloading article");
        }
    }

    /**
     * This task will download an attachment.
     */
    @WorkerThread
    @GuardedBy("mDownloadResourceLocks")
    void downloadResourceTask(@NonNull final Uri uri, final boolean force) {
        final ResourceDao resourceDao = mAemDb.resourceDao();

        // short-circuit if there isn't an Attachment for the specified Uri
        final Resource resource = resourceDao.find(uri);
        if (resource == null) {
            return;
        }

        // short-circuit if the Resource isn't stale and we aren't forcing a download
        if (!force && !resource.needsDownload()) {
            return;
        }

        // download the resource
        try {
            final Response<ResponseBody> response = mApi.downloadResource(uri).execute();
            final ResponseBody responseBody = response.body();
            if (response.code() == 200 && responseBody != null) {
                final File file = streamResource(responseBody.byteStream());
                if (file != null) {
                    resource.setContentType(responseBody.contentType());
                    resource.setLocalFileName(file.getName());
                    resource.setDateDownloaded(new Date());
                    resourceDao.updateLocalFile(resource.getUri(), resource.getContentType(),
                                                resource.getLocalFileName(), resource.getDateDownloaded());
                }
            }
        } catch (final IOException e) {
            Timber.tag(TAG)
                    .d(e, "Error downloading attachment %s", uri);
        }
    }

    @WorkerThread
    void cleanOrphanedFiles() {
        // lock the filesystem before removing any orphaned files
        final Lock lock = LOCK_FILESYSTEM.writeLock();
        try {
            lock.lock();

            // determine which files are still being referenced
            final Set<File> valid = Stream.of(mAemDb.resourceDao().getAll())
                    .map(r -> r.getLocalFile(mContext))
                    .collect(Collectors.toSet());

            // delete any files not referenced
            //noinspection ResultOfMethodCallIgnored
            Optional.ofNullable(ResourceUtilsKt.getResourcesDir(mContext).listFiles())
                    .map(Stream::of).stream().flatMap(s -> s)
                    .filterNot(valid::contains)
                    .forEach(File::delete);
        } finally {
            lock.unlock();
        }
    }

    // endregion Tasks

    @VisibleForTesting
    static long roundTimestamp(final long timestamp, final long interval) {
        // always round down for simplicity
        return timestamp - (timestamp % interval);
    }

    @WorkerThread
    private void downloadResourcesNeedingUpdate(@NonNull final List<Resource> resources) {
        Stream.of(resources)
                .filter(Resource::needsDownload)
                .forEach(r -> enqueueDownloadResource(r.getUri(), false));
    }

    @Nullable
    private File streamResource(@NonNull final InputStream in) throws IOException {
        // short-circuit if the resources directory isn't valid
        if (!ResourceUtilsKt.ensureResourcesDirExists(mContext)) {
            return null;
        }

        // create MessageDigest to dedup files
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            digest = null;
            Timber.tag(TAG)
                    .d(e, "Unable to create MessageDigest to dedup AEM resources");
        }

        // lock the file system for writing this resource
        final Lock lock = LOCK_FILESYSTEM.readLock();
        try {
            lock.lock();

            // stream resource to temporary file
            final File tmpFile = ResourceUtilsKt.createNewFile(mContext);
            final Closer closer = Closer.create();
            try {
                OutputStream out = closer.register(new FileOutputStream(tmpFile));
                if (digest != null) {
                    out = closer.register(new DigestOutputStream(out, digest));
                }
                IOUtils.copy(in, out);
                out.flush();
                out.close();
            } catch (final Throwable t) {
                throw closer.rethrow(t);
            } finally {
                closer.close();
            }

            // rename temporary file to name based on digest
            if (digest != null) {
                final String hash = HashCode.fromBytes(digest.digest()).toString() + ".bin";
                final File file = ResourceUtilsKt.getFile(mContext, hash);
                if (file.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    tmpFile.delete();
                    return file;
                } else if (tmpFile.renameTo(file)) {
                    return file;
                } else {
                    Timber.tag(TAG)
                            .d("cannot rename tmp file %s to %s", tmpFile, file);
                }
            }

            // default to returning the temporary file
            return tmpFile;
        } finally {
            lock.unlock();
        }
    }

    private class RoomDatabaseChangeTracker extends InvalidationTracker.Observer {
        RoomDatabaseChangeTracker(@NonNull final String firstTable, final String... rest) {
            super(firstTable, rest);
        }

        @Override
        public void onInvalidated(@NonNull final Set<String> tables) {
            for (final String table : tables) {
                switch (table) {
                    case TABLE_NAME_RESOURCE:
                        enqueueCleanOrphanedFiles();
                        break;
                }
            }
        }
    }

    // region PriorityRunnable Tasks

    private static final int PRIORITY_CLEANER = Integer.MIN_VALUE;
    private static final int PRIORITY_GENERATE_SHARE_LINK = -50;
    private static final int PRIORITY_SYNC_AEM_IMPORT = -40;
    private static final int PRIORITY_DOWNLOAD_RESOURCE = -30;
    private static final int PRIORITY_DOWNLOAD_ARTICLE = -20;

    abstract class UniqueTask implements PriorityRunnable {
        volatile boolean mForce = false;
        volatile boolean mStarted = false;
        final SettableFuture<Boolean> mResult = SettableFuture.create();

        /**
         * Update the force flag for this task, but only before it has started. We never go from forcing the sync to not
         * forcing it.
         *
         * @param force whether to force this sync to execute
         * @return true if the task hasn't started so the update was successful, false if the task has started.
         */
        final synchronized boolean updateTask(final boolean force) {
            if (!mStarted) {
                mForce = mForce || force;
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            synchronized (getLock()) {
                synchronized (this) {
                    mStarted = true;
                }
                mResult.set(runTask());
            }
        }

        @NonNull
        abstract Object getLock();

        abstract boolean runTask();
    }

    abstract class UniqueUriBasedTask extends UniqueTask {
        @NonNull
        final Uri mUri;

        UniqueUriBasedTask(@NonNull final Uri uri) {
            mUri = uri;
        }
    }

    class SyncAemImportTask extends UniqueUriBasedTask {
        SyncAemImportTask(@NonNull final Uri uri) {
            super(uri);
        }

        @Override
        public int getPriority() {
            return PRIORITY_SYNC_AEM_IMPORT;
        }

        @NonNull
        @Override
        Object getLock() {
            return ThreadUtils.getLock(mSyncAemImportLocks, mUri);
        }

        @Override
        boolean runTask() {
            syncAemImportTask(mUri, mForce);
            return true;
        }
    }

    class DownloadArticleTask extends UniqueUriBasedTask {
        DownloadArticleTask(@NonNull final Uri uri) {
            super(uri);
        }

        @Override
        public int getPriority() {
            return PRIORITY_DOWNLOAD_ARTICLE;
        }

        @NonNull
        @Override
        Object getLock() {
            return ThreadUtils.getLock(mDownloadArticleLocks, mUri);
        }

        @Override
        boolean runTask() {
            downloadArticleTask(mUri, mForce);
            return true;
        }
    }

    class DownloadResourceTask extends UniqueUriBasedTask {
        DownloadResourceTask(@NonNull final Uri uri) {
            super(uri);
        }

        @Override
        public int getPriority() {
            return PRIORITY_DOWNLOAD_RESOURCE;
        }

        @NonNull
        @Override
        Object getLock() {
            return ThreadUtils.getLock(mDownloadResourceLocks, mUri);
        }

        @Override
        boolean runTask() {
            downloadResourceTask(mUri, mForce);
            return true;
        }
    }

    class CleanOrphanedFilesTask extends UniqueTask {
        @Override
        public int getPriority() {
            return PRIORITY_CLEANER;
        }

        @NonNull
        @Override
        Object getLock() {
            return mCleanOrphanedFilesLock;
        }

        @Override
        boolean runTask() {
            cleanOrphanedFiles();
            scheduleCleanOrphanedFiles();
            return true;
        }
    }

    // endregion PriorityRunnable Tasks
}
