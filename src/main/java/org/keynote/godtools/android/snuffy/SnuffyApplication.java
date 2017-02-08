package org.keynote.godtools.android.snuffy;

import android.os.Environment;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.RenderAppConfig;
import org.keynote.godtools.renderer.crureader.BaseAppConfig;
import org.keynote.godtools.renderer.crureader.RenderApp;

import java.io.File;

import io.fabric.sdk.android.Fabric;

public class SnuffyApplication extends RenderApp {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable crash reporting
        Fabric.with(this, new Crashlytics());
        NewRelic.withApplicationToken(BuildConfig.NEW_RELIC_API_KEY).start(this);
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
}
