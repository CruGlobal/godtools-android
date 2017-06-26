package org.keynote.godtools.android.snuffy;

import android.os.Environment;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.newrelic.agent.android.NewRelic;
import com.squareup.picasso.Picasso;

import org.ccci.gto.android.common.api.okhttp3.util.OkHttpClientUtil;
import org.cru.godtools.sync.service.FollowupService;
import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.RenderAppConfig;
import org.keynote.godtools.android.service.GodToolsToolManager;
import org.keynote.godtools.renderer.crureader.BaseAppConfig;
import org.keynote.godtools.renderer.crureader.RenderApp;

import java.io.File;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;

public class SnuffyApplication extends RenderApp {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable crash reporting
        Fabric.with(this, new Crashlytics());
        NewRelic.withApplicationToken(BuildConfig.NEW_RELIC_API_KEY).start(this);

        // initialize several support libraries
        Picasso.setSingletonInstance(picassoBuilder().build());

        // Initialize tool manager
        GodToolsToolManager.getInstance(this);
        FollowupService.start(this);
    }

    @Override
    public BaseAppConfig getBaseAppConfig() {
        return new RenderAppConfig();
    }

    public File getDocumentsDir() {
        File documentsDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            documentsDir = getExternalFilesDir(null);
            if (documentsDir != null) {
                Crashlytics.log("documentsDir: " + documentsDir.getPath());
                if (!documentsDir.isDirectory()) {
                    Crashlytics.log("documentsDir doesn't exist");
                    if (!documentsDir.mkdirs()) {
                        Crashlytics.log("unable to create documents directory, falling back to internal directory");
                        documentsDir = null;
                    }
                }
            }
        }
        if (documentsDir == null) {
            documentsDir = getFilesDir();
        }
        return documentsDir;
    }

    @NonNull
    protected Picasso.Builder picassoBuilder() {
        final Picasso.Builder builder = new Picasso.Builder(this);

        // use OkHttp3 for the downloader
        final OkHttpClient okhttp = OkHttpClientUtil.attachGlobalInterceptors(new OkHttpClient.Builder())
                .cache(OkHttp3Downloader.createDefaultCache(this))
                .build();
        builder.downloader(new OkHttp3Downloader(okhttp));

        return builder;
    }
}
