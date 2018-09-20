package org.cru.godtools.articles.aem.service;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.annimon.stream.Stream;
import com.google.common.util.concurrent.Futures;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.cru.godtools.articles.aem.db.ArticleRepository;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.db.TranslationRepository;
import org.cru.godtools.articles.aem.model.AemImport;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.service.support.ArticleParser;
import org.cru.godtools.base.util.PriorityRunnable;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.service.ManifestManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * This class hold all Download methods for retrieving and saving an Article
 *
 * @author Gyasi Story
 */
public class AEMDownloadManger {
    private static final int TASK_CONCURRENCY = 4;

    private final ArticleRoomDatabase mAemDb;
    private final GodToolsDao mDao;
    private final ThreadPoolExecutor mExecutor;
    private final ManifestManager mManifestManager;
    private final Context mContext;

    // Task synchronization locks and flags
    private final Object mExtractAemImportsLock = new Object();
    private final AtomicBoolean mExtractAemImportsQueued = new AtomicBoolean(false);

    // Task de-dup related objects
    private final Map<Uri, SyncAemImportTask> mSyncAemImportTasks = Collections.synchronizedMap(new HashMap<>());

    private AEMDownloadManger(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mAemDb = ArticleRoomDatabase.getInstance(mContext);
        mDao = GodToolsDao.getInstance(mContext);
        mExecutor = new ThreadPoolExecutor(0, TASK_CONCURRENCY, 10, TimeUnit.SECONDS,
                                           new PriorityBlockingQueue<>(11, PriorityRunnable.COMPARATOR),
                                           new NamedThreadFactory(AEMDownloadManger.class.getSimpleName()));
        mManifestManager = ManifestManager.getInstance(mContext);

        EventBus.getDefault().register(this);
    }

    @Nullable
    private static AEMDownloadManger sInstance;

    @NonNull
    public static synchronized AEMDownloadManger getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            sInstance = new AEMDownloadManger(context);
        }
        return sInstance;
    }

    // region Lifecycle Events

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        enqueueExtractAemImportsFromManifests();
    }

    // endregion Lifecycle Events

    // region Task Scheduling Methods

    @AnyThread
    private void enqueueExtractAemImportsFromManifests() {
        // only enqueue task if it's not currently enqueued
        if (!mExtractAemImportsQueued.getAndSet(true)) {
            mExecutor.execute(() -> {
                extractAemImportsFromManifestsTask();
                enqueueSyncStaleAemImports();
            });
        }
    }

    @AnyThread
    private void enqueueSyncStaleAemImports() {
        mExecutor.execute(this::syncStaleAemImportsTask);
    }

    @AnyThread
    private void enqueueSyncAemImport(@NonNull final Uri uri, final boolean force) {
        // try updating a task that is currently enqueued
        final SyncAemImportTask existing = mSyncAemImportTasks.get(uri);
        if (existing != null && existing.updateTask(force)) {
            return;
        }

        // create a new sync task
        final SyncAemImportTask task = new SyncAemImportTask(uri);
        task.updateTask(force);
        mSyncAemImportTasks.put(uri, task);
        mExecutor.execute(task);
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

                // add AEM imports extracted from the manifest to the AEM article cache
                final Manifest manifest = Futures.getUnchecked(mManifestManager.getManifest(translation));
                repository.addAemImports(translation, manifest.getAemImports());
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
                .filterNot(AemImport::isStale)
                .forEach(i -> enqueueSyncAemImport(i.uri, false));
    }

    /**
     * This task is responsible for syncing an individual AEM Import url to the AEM Article database.
     *
     * @param baseUri The base AEM Import URL to sync
     * @param force   This flag indicates that this sync should ignore the lastUpdated time
     */
    @WorkerThread
    void syncAemImportTask(@NonNull final Uri baseUri, final boolean force) {
        if (!force) {
            AemImport aemImport = mAemDb.aemImportDao().getAemImport(baseUri);
            if (aemImport != null) {
                if (!aemImport.isStale()) {
                    return; // Don't import Aem if not forced and aemImport is not stale
                }
            }
        }
        try {
            loadAemManifestIntoAemModel(baseUri);
        } catch (JSONException | IOException e) {
            Timber.tag("syncAemImportTask").e(e);
        }
    }

    /**
     * This task will download the html content for a specific article.
     */
    @WorkerThread
    void downloadArticleTask() {
        // TODO
    }

    /**
     * This task will download an attachment.
     */
    @WorkerThread
    void downloadAttachmentTask() {
        // TODO
    }

    /**
     * This task will remove any attachments no longer in use.
     */
    @WorkerThread
    void pruneOldAttachmentsTask() {
        // TODO
    }

    // endregion Tasks

    /**
     * This method take the manifest and one of its aemImports and extracts all associated data to
     * the database.
     *
     * @param aemImports uri from one of the aemImports
     * @throws JSONException
     * @throws IOException
     */
    private void loadAemManifestIntoAemModel(Uri aemImports)
            throws JSONException, IOException {

        ArticleRepository articleRepository = new ArticleRepository(mContext);
        AttachmentRepository attachmentRepository = new AttachmentRepository(mContext);

        JSONObject importJson = getJsonFromUri(aemImports);

        final List<Article> articles = ArticleParser.parse(importJson);

        for (Article createdArticle : articles) {
            // Save Article
            articleRepository.insertArticle(createdArticle);

            if (createdArticle.parsedAttachments != null) {
                for (final Attachment attachment : createdArticle.parsedAttachments) {
                    attachmentRepository.insertAttachment(attachment);
                }
            }
        }
    }

    /**
     * Gets JSON Object out of Uri
     *
     * @param aemImports uri
     * @return JSON object from the Uri
     * @throws JSONException
     * @throws IOException
     */
    private JSONObject getJsonFromUri(Uri aemImports)
            throws JSONException, IOException {

        // Have to convert android Uri to a Java URI
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(aemImports.toString())
                .build();
        Response response = client.newCall(request).execute();
        return new JSONObject(response.body().string());
    }

    /**
     * This method is used to save an Attachment to Storage and update Database
     *
     * @param attachment the attachment to be saved
     * @throws IOException Is thrown if an error occurs in saving to storage.
     */
    public void saveAttachmentToStorage(Attachment attachment)
            throws IOException {
        // verify that attachment is not already saved.
        if (attachment.mAttachmentFilePath != null) {
            //TODO: determine what should happen
        } else {
            String[] urlSplit = attachment.mAttachmentUrl.split("/");
            String filename = urlSplit[urlSplit.length - 1];
            File articleFile = new File(mContext.getFilesDir(), "articles");
            if (!articleFile.exists()) {
                articleFile.mkdir();
            }
            articleFile = new File(articleFile, filename);
            FileOutputStream outputStream = new FileOutputStream(articleFile);
            URL url = new URL(attachment.mAttachmentUrl);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            outputStream.write(client.newCall(request).execute().body().bytes());

            // update attachment with file Path
            attachment.mAttachmentFilePath = articleFile.getAbsolutePath();
            AttachmentRepository repository = new AttachmentRepository(mContext);
            repository.updateAttachment(attachment);
        }
    }

    // region PriorityRunnable Tasks

    private static final int PRIORITY_SYNC_AEM_IMPORT = -30;

    class SyncAemImportTask extends PriorityRunnable {
        @NonNull
        private final Uri mUri;
        private volatile boolean mStarted = false;
        private volatile boolean mForce = false;

        SyncAemImportTask(@NonNull final Uri uri) {
            mUri = uri;
        }

        @Override
        protected int getPriority() {
            return PRIORITY_SYNC_AEM_IMPORT;
        }

        /**
         * Update the force flag for this task, but only before it has started. We never go from forcing the sync to not
         * forcing it.
         *
         * @param force whether to force this sync to execute
         * @return true if the task hasn't started so the update was successful, false if the task has started.
         */
        synchronized boolean updateTask(final boolean force) {
            if (!mStarted) {
                mForce = mForce || force;
                return true;
            }
            return false;
        }

        @Override
        public void run() {
            synchronized (this) {
                mStarted = true;
            }
            syncAemImportTask(mUri, mForce);
        }
    }

    // endregion PriorityRunnable Tasks
}
