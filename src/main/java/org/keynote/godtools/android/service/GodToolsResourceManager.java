package org.keynote.godtools.android.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.eventbus.task.EventBusDelayedPost;
import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.android.Settings;
import org.keynote.godtools.android.db.Contract;
import org.keynote.godtools.android.db.Contract.LanguageTable;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.event.LanguageUpdateEvent;
import org.keynote.godtools.android.event.ResourceUpdateEvent;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Resource;

import java.util.Locale;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public final class GodToolsResourceManager {
    private final Context mContext;
    private final GodToolsDao mDao;
    private final Settings mPrefs;

    private GodToolsResourceManager(@NonNull final Context context) {
        mContext = context;
        mDao = GodToolsDao.getInstance(mContext);
        mPrefs = Settings.getInstance(mContext);
    }

    @Nullable
    @SuppressLint("StaticFieldLeak")
    private static GodToolsResourceManager sInstance;
    @NonNull
    public static GodToolsResourceManager getInstance(@NonNull final Context context) {
        synchronized (GodToolsResourceManager.class) {
            if (sInstance == null) {
                sInstance = new GodToolsResourceManager(context.getApplicationContext());
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

    public void addResource(final long id) {
        final Resource resource = new Resource();
        resource.setId(id);
        resource.setAdded(true);
        final ListenableFuture<Integer> update = mDao.updateAsync(resource, Contract.ResourceTable.COLUMN_ADDED);
        update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new ResourceUpdateEvent()), directExecutor());
    }

    public void removeResource(final long id) {
        final Resource resource = new Resource();
        resource.setId(id);
        resource.setAdded(false);
        final ListenableFuture<Integer> update = mDao.updateAsync(resource, Contract.ResourceTable.COLUMN_ADDED);
        update.addListener(new EventBusDelayedPost(EventBus.getDefault(), new ResourceUpdateEvent()), directExecutor());
    }
}
