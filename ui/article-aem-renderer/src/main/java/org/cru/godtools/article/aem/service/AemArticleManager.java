package org.cru.godtools.article.aem.service;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.cru.godtools.article.aem.api.AemApi;
import org.cru.godtools.article.aem.db.ArticleRoomDatabase;
import org.cru.godtools.article.aem.db.TranslationRepository;
import org.cru.godtools.article.aem.util.AemFileManager;
import org.cru.godtools.base.tool.service.ManifestManager;
import org.cru.godtools.base.util.PriorityRunnable;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.cru.godtools.xml.model.Manifest;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

/**
 * This class hold all the logic for maintaining a local cache of AEM Articles.
 */
@Singleton
public class AemArticleManager extends KotlinAemArticleManager {
    private static final int TASK_CONCURRENCY = 10;

    private final ArticleRoomDatabase mAemDb;
    private final GodToolsDao mDao;
    private final ThreadPoolExecutor mExecutor;
    private final ManifestManager mManifestManager;

    // Task synchronization locks and flags
    private final Object mExtractAemImportsLock = new Object();
    private final AtomicBoolean mExtractAemImportsQueued = new AtomicBoolean(false);

    @Inject
    AemArticleManager(final EventBus eventBus, final GodToolsDao dao, final AemApi api,
                      final ManifestManager manifestManager, final ArticleRoomDatabase aemDb,
                      final AemFileManager fileManager) {
        super(aemDb, api, fileManager);
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
                        syncAemImportsFromManifestAsync(manifest, false);
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
    // endregion Tasks
}
