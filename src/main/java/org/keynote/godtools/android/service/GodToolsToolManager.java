package org.keynote.godtools.android.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.google.common.io.Closer;
import com.google.common.io.CountingInputStream;
import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.eventbus.task.EventBusDelayedPost;
import org.ccci.gto.android.common.util.IOUtils;
import org.ccci.gto.android.common.util.IOUtils.ProgressCallback;
import org.ccci.gto.android.common.util.ThreadUtils;
import org.greenrobot.eventbus.EventBus;
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
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public final class GodToolsToolManager {
    private static final ArrayMap<String, Object> LOCKS_FILES = new ArrayMap<>();

    private final Context mContext;
    private final GodToolsApi mApi;
    private final GodToolsDao mDao;
    private final EventBus mEventBus;
    private final Settings mPrefs;

    private GodToolsToolManager(@NonNull final Context context) {
        mContext = context;
        mApi = GodToolsApi.getInstance(mContext);
        mDao = GodToolsDao.getInstance(mContext);
        mEventBus = EventBus.getDefault();
        mPrefs = Settings.getInstance(mContext);
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
    public void downloadTranslation(final long toolId, @NonNull final Locale language) {
        final Query<Translation> query = Query.select(Translation.class)
                .where(TranslationTable.SQL_WHERE_TOOL_LANGUAGE.args(toolId, language))
                .orderBy(TranslationTable.COLUMN_VERSION + " DESC");
        final Translation translation = mDao.streamCompat(query)
                .filter(Translation::isPublished)
                .findFirst()
                .orElse(null);
        if (translation != null && !translation.isDownloaded()) {
            try {
                final Response<ResponseBody> response = mApi.translations.download(translation.getId()).execute();
                if (response.isSuccessful()) {
                    final ResponseBody body = response.body();
                    if (body != null) {
                        processDownload(translation, body);

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

    /**
     * Process a streaming zip response.
     */
    private void processDownload(@NonNull final Translation translation, @NonNull final ResponseBody body)
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
                synchronized (ThreadUtils.getLock(LOCKS_FILES, fileName)) {
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
}
