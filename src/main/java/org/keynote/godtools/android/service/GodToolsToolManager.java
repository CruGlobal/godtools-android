package org.keynote.godtools.android.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.eventbus.task.EventBusDelayedPost;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.Settings;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.event.LanguageUpdateEvent;
import org.keynote.godtools.android.event.ToolUpdateEvent;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public final class GodToolsToolManager {
    private final Context mContext;
    private final GodToolsDao mDao;
    private final Settings mPrefs;

    private GodToolsToolManager(@NonNull final Context context) {
        mContext = context;
        mDao = GodToolsDao.getInstance(mContext);
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
}
