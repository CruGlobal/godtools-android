package org.cru.godtools.articles.aem.service;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.common.util.concurrent.Futures;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.cru.godtools.articles.aem.db.ArticleRepository;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.db.AttachmentRepository;
import org.cru.godtools.articles.aem.db.ManifestAssociationRepository;
import org.cru.godtools.articles.aem.db.TranslationRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.model.Attachment;
import org.cru.godtools.articles.aem.model.ManifestAssociation;
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
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

    // Task synchronization locks and flags
    private final Object mExtractAemImportsLock = new Object();
    private final AtomicBoolean mExtractAemImportsQueued = new AtomicBoolean(false);

    private AEMDownloadManger(@NonNull final Context context) {
        mAemDb = ArticleRoomDatabase.getInstance(context);
        mDao = GodToolsDao.getInstance(context);
        mExecutor = new ThreadPoolExecutor(0, TASK_CONCURRENCY, 10, TimeUnit.SECONDS,
                                           new PriorityBlockingQueue<>(11, PriorityRunnable.COMPARATOR),
                                           new NamedThreadFactory(AEMDownloadManger.class.getSimpleName()));
        mManifestManager = ManifestManager.getInstance(context);

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
            mExecutor.execute(new ExtractAemImportsFromManifestsTask());
        }
    }

    @AnyThread
    private void enqueueStaleAemImport() {
        // TODO
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

            // TODO: prune any translations that we no longer have downloaded.
        }
    }

    /**
     * This task is responsible for triggering syncs of any stale Aem Imports
     */
    @WorkerThread
    void syncStaleAemImports() {
        // TODO
    }

    /**
     * This task is responsible for syncing an individual AEM Import url to the AEM Article database.
     *
     * @param baseUri The base AEM Import URL to sync
     * @param force   This flag indicates that this sync should ignore the lastUpdated time
     */
    @WorkerThread
    void syncAemImportTask(@NonNull final Uri baseUri, final boolean force) {
        // TODO
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
     * This method  handles loading AEM Imports into the local Database.  Please ensure that the
     * manifest used contains AemImports.
     *
     * @param manifest the model for the manifest
     * @param context  the Application Context
     * @throws JSONException
     * @throws IOException
     */
    @WorkerThread
    public static void loadAEMFromManifest(final Manifest manifest, Context context)
            throws JSONException, IOException {

        // Verify that Manifest has AEM Articles (Should be checked already
        if (manifest.getAemImports() == null || manifest.getAemImports().size() <= 0) {

            return;  // May need to throw an error to limit unnecessary calls.
        }

        for (Uri aemImports : manifest.getAemImports()) {
            loadAemManifestIntoAemModel(manifest, aemImports, context);
        }

    }

    /**
     * This method take the manifest and one of its aemImports and extracts all associated data to
     * the database.
     *
     * @param manifest   manifest object
     * @param aemImports uri from one of the aemImports
     * @param context    the Application or Activity Context
     * @throws JSONException
     * @throws IOException
     */
    private static void loadAemManifestIntoAemModel(Manifest manifest, Uri aemImports, Context context)
            throws JSONException, IOException {

        ArticleRepository articleRepository = new ArticleRepository(context);
        AttachmentRepository attachmentRepository = new AttachmentRepository(context);
        ManifestAssociationRepository manifestAssociationRepository =
                new ManifestAssociationRepository(context);

        JSONObject importJson = getJsonFromUri(aemImports);

        final List<Article> articles = ArticleParser.parse(importJson);

        for (Article createdArticle : articles) {
            ManifestAssociation createdAssociation = new ManifestAssociation();
            createdAssociation.mManifestId = manifest.getCode();
            createdAssociation.mManifestName = manifest.getManifestName();
            createdAssociation.mArticleId = createdArticle.mkey;

            // Save Association
            manifestAssociationRepository.insertAssociation(createdAssociation);

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
    private static JSONObject getJsonFromUri(Uri aemImports)
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
     * @param context    The context is needed to save the attachment and database entry
     * @throws IOException Is thrown if an error occurs in saving to storage.
     */
    public static void saveAttachmentToStorage(Attachment attachment, Context context)
            throws IOException {
        // verify that attachment is not already saved.
        if (attachment.mAttachmentFilePath != null) {
            //TODO: determine what should happen
        } else {
            String[] urlSplit = attachment.mAttachmentUrl.split("/");
            String filename = urlSplit[urlSplit.length - 1];
            File articleFile = new File(context.getFilesDir(), "articles");
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
            AttachmentRepository repository = new AttachmentRepository(context);
            repository.updateAttachment(attachment);
        }
    }

    // region Task PriorityRunnables

    class ExtractAemImportsFromManifestsTask extends PriorityRunnable {
        @Override
        public void run() {
            extractAemImportsFromManifestsTask();
        }
    }

    // endregion Task PriorityRunnables
}
