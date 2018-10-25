package org.cru.godtools;

import android.os.AsyncTask;

import com.evernote.android.job.JobManager;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.base.app.BaseGodToolsApplication;
import org.cru.godtools.download.manager.DownloadManagerEventBusIndex;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.init.content.task.InitialContentTasks;
import org.cru.godtools.model.event.ModelEventEventBusIndex;
import org.cru.godtools.model.loader.ModelLoaderEventBusIndex;
import org.cru.godtools.service.AccountListRegistrationService;
import org.cru.godtools.shortcuts.GodToolsShortcutManager;
import org.cru.godtools.shortcuts.ShortcutsEventBusIndex;
import org.cru.godtools.sync.job.SyncJobCreator;
import org.cru.godtools.tract.TractEventBusIndex;
import org.cru.godtools.tract.service.FollowupService;
import org.greenrobot.eventbus.EventBusBuilder;

import androidx.annotation.NonNull;
import me.thekey.android.core.TheKeyImpl;
import me.thekey.android.eventbus.EventBusEventsManager;

import static org.cru.godtools.account.BuildConfig.ACCOUNT_TYPE;
import static org.cru.godtools.account.BuildConfig.THEKEY_CLIENTID;
import static org.cru.godtools.config.BuildConfig.MOBILE_CONTENT_API;

public class GodToolsApplication extends BaseGodToolsApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // start the Job Manager
        JobManager.create(this).addJobCreator(new SyncJobCreator());

        // install any missing initial content
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new InitialContentTasks(this));
    }

    @Override
    protected void configureApis() {
        GodToolsApi.configure(this, MOBILE_CONTENT_API);
    }

    @NonNull
    protected EventBusBuilder configureEventBus(@NonNull final EventBusBuilder builder) {
        return super.configureEventBus(builder)
                .addIndex(new AppEventBusIndex())
                .addIndex(new DownloadManagerEventBusIndex())
                .addIndex(new ModelEventEventBusIndex())
                .addIndex(new ModelLoaderEventBusIndex())
                .addIndex(new ShortcutsEventBusIndex())
                .addIndex(new TractEventBusIndex());
    }

    @Override
    protected void configureTheKey() {
        TheKeyImpl.configure(theKeyConfiguration());
    }

    @Override
    protected void startServices() {
        super.startServices();
        GodToolsDownloadManager.getInstance(this);
        GodToolsShortcutManager.getInstance(this);
        AccountListRegistrationService.start(this);
        FollowupService.start(this);
    }

    private TheKeyImpl.Configuration theKeyConfiguration() {
        return TheKeyImpl.Configuration.base()
                .accountType(ACCOUNT_TYPE)
                .clientId(THEKEY_CLIENTID)
                .eventsManager(new EventBusEventsManager());
    }
}
