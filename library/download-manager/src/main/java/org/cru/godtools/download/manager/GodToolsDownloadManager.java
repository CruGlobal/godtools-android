package org.cru.godtools.download.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.concurrent.NamedThreadFactory;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.task.EventBusDelayedPost;
import org.cru.godtools.api.AttachmentsApi;
import org.cru.godtools.api.TranslationsApi;
import org.cru.godtools.base.FileManager;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.util.FileUtils;
import org.cru.godtools.base.util.PriorityRunnable;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.TranslationKey;
import org.cru.godtools.model.event.AttachmentUpdateEvent;
import org.cru.godtools.model.event.LanguageUpdateEvent;
import org.cru.godtools.model.event.ToolUpdateEvent;
import org.cru.godtools.model.event.TranslationUpdateEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.db.Contract.AttachmentTable;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.LocalFileTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.Contract.TranslationTable;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.collection.ArraySet;
import androidx.collection.LongSparseArray;
import dagger.hilt.android.qualifiers.ApplicationContext;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.ccci.gto.android.common.db.Expression.NULL;
import static org.ccci.gto.android.common.util.ThreadUtils.getLock;

@Singleton
@SuppressLint("VisibleForTests")
public final class GodToolsDownloadManager extends KotlinGodToolsDownloadManager {
    private static final int DOWNLOAD_CONCURRENCY = 4;

    private final Context mContext;
    private final TranslationsApi mTranslationsApi;
    private final GodToolsDao mDao;
    private final EventBus mEventBus;
    final Settings mPrefs;
    private final ThreadPoolExecutor mExecutor;

    final LongSparseArray<Boolean> mDownloadingAttachments = new LongSparseArray<>();

    @Inject
    GodToolsDownloadManager(@ApplicationContext @NonNull final Context context,
                            @NonNull final AttachmentsApi attachmentsApi,
                            @NonNull final TranslationsApi translationsApi, @NonNull final GodToolsDao dao,
                            @NonNull final EventBus eventBus, @NonNull final FileManager fileManager,
                            @NonNull final Settings settings) {
        super(attachmentsApi, dao, eventBus, fileManager);
        mContext = context;
        mTranslationsApi = translationsApi;
        mDao = dao;
        mEventBus = eventBus;
        mPrefs = settings;
        mExecutor = new ThreadPoolExecutor(0, DOWNLOAD_CONCURRENCY, 10, TimeUnit.SECONDS,
                                           new PriorityBlockingQueue<>(11, PriorityRunnable.COMPARATOR),
                                           new NamedThreadFactory(GodToolsDownloadManager.class.getSimpleName()));

        // register with EventBus
        mEventBus.register(this);
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
        enqueueToolBannerAttachments();
        enqueuePendingPublishedTranslations();
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onTranslationUpdate(@NonNull final TranslationUpdateEvent event) {
        enqueuePendingPublishedTranslations();
    }

    // endregion Lifecycle Events

    @AnyThread
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
            update.addListener(this::pruneStaleTranslations, directExecutor());
            update.addListener(new EventBusDelayedPost(mEventBus, LanguageUpdateEvent.INSTANCE),
                               directExecutor());
        }
    }

    @AnyThread
    public void removeTool(@NonNull final String code) {
        final Tool tool = new Tool();
        tool.setCode(code);
        tool.setAdded(false);
        final ListenableFuture<Integer> update = mDao.updateAsync(tool, ToolTable.COLUMN_ADDED);
        update.addListener(this::pruneStaleTranslations, directExecutor());
        update.addListener(new EventBusDelayedPost(mEventBus, ToolUpdateEvent.INSTANCE), directExecutor());
    }

    @AnyThread
    public void cacheTranslation(@NonNull final String code, @NonNull final Locale locale) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> mDao.streamCompat(
                Query.select(Translation.class)
                        .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(code, locale)
                                       .and(TranslationTable.SQL_WHERE_PUBLISHED))
                        .orderBy(TranslationTable.SQL_ORDER_BY_VERSION_DESC))
                .findFirst()
                .executeIfPresent(t -> {
                    t.updateLastAccessed();
                    mDao.update(t, TranslationTable.COLUMN_LAST_ACCESSED);
                })
                .map(TranslationKey::new)
                .map(DownloadTranslationRunnable::new)
                .ifPresent(mExecutor::execute));
    }

    @WorkerThread
    void downloadLatestPublishedTranslation(@NonNull final TranslationKey key) {
        // short-circuit if the resources directory isn't valid
        if (!FileUtils.createGodToolsResourcesDir(mContext)) {
            return;
        }

        // lock translation
        synchronized (getLock(LOCKS_TRANSLATION_DOWNLOADS, key)) {
            // process the most recent published version
            final Query<Translation> query = Query.select(Translation.class)
                    .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(key.getTool(), key.getLocale())
                                   .and(TranslationTable.SQL_WHERE_PUBLISHED))
                    .orderBy(TranslationTable.COLUMN_VERSION + " DESC")
                    .limit(1);
            final Translation translation = mDao.streamCompat(query).findFirst().orElse(null);

            // only process this translation if it's not already downloaded
            if (translation != null && !translation.isDownloaded()) {
                // track the start of this download
                startProgress(key);

                try {
                    final Response<ResponseBody> response = mTranslationsApi.download(translation.getId()).execute();
                    if (response.isSuccessful()) {
                        final ResponseBody body = response.body();
                        if (body != null) {
                            storeTranslation(translation, body.byteStream(), body.contentLength());

                            // prune any old translations
                            pruneStaleTranslations();
                        }
                    }
                } catch (final IOException ignored) {
                }
            }

            // We always finish the download (even if we didn't start it) because of the following race condition:
            //
            // [1] enqueuePendingPublishedTranslations() loads the list of pending downloads from the database
            // [2] downloadLatestPublishedTranslation() finishes downloading one of the translations loaded by [1]
            // [1] enqueuePendingPublishedTranslations() triggers startProgress() on already downloaded translation
            // [1] downloadLatestPublishedTranslation() short-circuits on the actual download logic
            // [1] we still need to call finishDownload()
            finishDownload(key);
        }
    }

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

    @WorkerThread
    private void enqueueToolBannerAttachments() {
        mDao.streamCompat(Query.select(Attachment.class)
                                  .join(AttachmentTable.SQL_JOIN_TOOL.andOn(
                                          ToolTable.FIELD_DETAILS_BANNER.eq(AttachmentTable.FIELD_ID)
                                                  .or(ToolTable.FIELD_BANNER.eq(AttachmentTable.FIELD_ID))))
                                  .where(AttachmentTable.FIELD_DOWNLOADED.eq(false)))
                .mapToLong(Attachment::getId)
                .forEach(this::enqueueAttachmentDownload);
    }

    @AnyThread
    private void enqueueAttachmentDownload(final long attachmentId) {
        synchronized (mDownloadingAttachments) {
            if (!mDownloadingAttachments.get(attachmentId, false)) {
                mExecutor.execute(new DownloadAttachmentRunnable(attachmentId));
                mDownloadingAttachments.put(attachmentId, true);
            }
        }
    }
    // endregion Download & Cleaning Scheduling Methods

    // region Task PriorityRunnables
    private static final int PRIORITY_PRIMARY = -40;
    private static final int PRIORITY_ATTACHMENT = -30;
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

    final class DownloadAttachmentRunnable implements PriorityRunnable {
        private final long mAttachmentId;

        DownloadAttachmentRunnable(final long attachmentId) {
            mAttachmentId = attachmentId;
        }

        @Override
        public int getPriority() {
            return PRIORITY_ATTACHMENT;
        }

        @Override
        public void run() {
            downloadAttachment(mAttachmentId);
            synchronized (mDownloadingAttachments) {
                mDownloadingAttachments.remove(mAttachmentId);
            }
        }
    }
    // endregion Task PriorityRunnables
}
