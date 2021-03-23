package org.cru.godtools.article.aem.service;

import android.net.Uri;

import com.annimon.stream.Stream;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.util.ThreadUtils;
import org.cru.godtools.article.aem.api.AemApi;
import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.db.TranslationRepository;
import org.cru.godtools.article.aem.model.AemImport;
import org.cru.godtools.article.aem.model.Article;
import org.cru.godtools.article.aem.model.Resource;
import org.cru.godtools.article.aem.service.support.AemJsonParserKt;
import org.cru.godtools.article.aem.service.support.HtmlParserKt;
import org.cru.godtools.article.aem.util.AemFileManager;
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

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import kotlin.sequences.SequencesKt;
import retrofit2.Response;
import timber.log.Timber;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.util.Collections.synchronizedMap;
import static org.ccci.gto.android.common.base.TimeConstants.HOUR_IN_MS;

/**
 * This class hold all the logic for maintaining a local cache of AEM Articles.
 */
@Singleton
public class AemArticleManager extends KotlinAemArticleManager {
    private static final String TAG = "AemArticleManager";

    private static final int TASK_CONCURRENCY = 10;
    private static final long CACHE_BUSTING_INTERVAL_JSON = HOUR_IN_MS;

    private final ArticleRoomDatabase mAemDb;
    private final AemApi mApi;
    private final GodToolsDao mDao;
    private final ThreadPoolExecutor mExecutor;
    private final ManifestManager mManifestManager;

    // Task synchronization locks and flags
    private final Object mExtractAemImportsLock = new Object();
    private final AtomicBoolean mExtractAemImportsQueued = new AtomicBoolean(false);
    final Map<Uri, Object> mSyncAemImportLocks = new HashMap<>();
    final Map<Uri, Object> mDownloadArticleLocks = new HashMap<>();

    // Task de-dup related objects
    private final Map<Uri, SyncAemImportTask> mSyncAemImportTasks = synchronizedMap(new HashMap<>());
    private final Map<Uri, DownloadArticleTask> mDownloadArticleTasks = synchronizedMap(new HashMap<>());

    @Inject
    AemArticleManager(final EventBus eventBus, final GodToolsDao dao, final AemApi api,
                      final ManifestManager manifestManager, final ArticleRoomDatabase aemDb,
                      final AemFileManager fileManager) {
        super(aemDb, api, fileManager);
        mApi = api;
        mAemDb = aemDb;
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

    // region PriorityRunnable Tasks
    private static final int PRIORITY_SYNC_AEM_IMPORT = -40;
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
    // endregion PriorityRunnable Tasks
}
