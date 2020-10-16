package org.cru.godtools.download.manager;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.cru.godtools.api.AttachmentsApi;
import org.cru.godtools.api.TranslationsApi;
import org.cru.godtools.base.FileManager;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.util.PriorityRunnable;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationKey;
import org.cru.godtools.model.event.LanguageUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Locale;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

@Singleton
@SuppressLint("VisibleForTests")
public final class GodToolsDownloadManager extends KotlinGodToolsDownloadManager {
    private static final int DOWNLOAD_CONCURRENCY = 4;

    private final GodToolsDao mDao;
    final Settings mPrefs;
    private final ThreadPoolExecutor mExecutor;

    @Inject
    GodToolsDownloadManager(@NonNull final AttachmentsApi attachmentsApi,
                            @NonNull final TranslationsApi translationsApi, @NonNull final GodToolsDao dao,
                            @NonNull final EventBus eventBus, @NonNull final FileManager fileManager,
                            @NonNull final Settings settings) {
        super(attachmentsApi, dao, eventBus, fileManager, settings, translationsApi);
        mDao = dao;
        mPrefs = settings;
        mExecutor = new ThreadPoolExecutor(0, DOWNLOAD_CONCURRENCY, 10, TimeUnit.SECONDS,
                                           new PriorityBlockingQueue<>(11, PriorityRunnable.COMPARATOR),
                                           new NamedThreadFactory(GodToolsDownloadManager.class.getSimpleName()));

        // register with EventBus
        eventBus.register(this);
    }

    // region Lifecycle Events

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

    // endregion Lifecycle Events

    // region Download & Cleaning Scheduling Methods
    @WorkerThread
    private void enqueuePendingPublishedTranslations() {
        final Query<Translation> query = Query.select(Translation.class)
                .joins(TranslationTable.SQL_JOIN_LANGUAGE, TranslationTable.SQL_JOIN_TOOL)
                .where(LanguageTable.SQL_WHERE_ADDED
                               .and(ToolTable.FIELD_ADDED.eq(true))
                               .and(TranslationTable.SQL_WHERE_PUBLISHED)
                               .and(TranslationTable.FIELD_DOWNLOADED.eq(false)))
                .orderBy(TranslationTable.COLUMN_VERSION + " DESC");

        mDao.streamCompat(query)
                .distinctBy(TranslationKey::new)
                .filterNot(Translation::isDownloaded)
                .map(TranslationKey::new)
                .peek(this::startProgress)
                .map(DownloadTranslationRunnable::new)
                .forEach(mExecutor::execute);
    }
    // endregion Download & Cleaning Scheduling Methods

    // region Task PriorityRunnables
    private static final int PRIORITY_PRIMARY = -40;
    private static final int PRIORITY_PARALLEL = -20;
    private static final int PRIORITY_OTHER = -10;

    final class DownloadTranslationRunnable implements PriorityRunnable {
        @NonNull
        final TranslationKey mKey;
        final int mPriority;

        DownloadTranslationRunnable(@NonNull final TranslationKey key) {
            mKey = key;
            final Locale primary = mPrefs.getPrimaryLanguage();
            final Locale parallel = mPrefs.getParallelLanguage();
            mPriority = primary.equals(mKey.getLocale()) ? PRIORITY_PRIMARY :
                    mKey.getLocale().equals(parallel) ? PRIORITY_PARALLEL : PRIORITY_OTHER;
        }

        @Override
        public int getPriority() {
            return mPriority;
        }

        @Override
        public void run() {
            downloadLatestPublishedTranslation(mKey);
        }
    }
    // endregion Task PriorityRunnables
}
