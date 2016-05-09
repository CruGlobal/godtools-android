package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.FileUtils;

import java.io.File;
import java.util.List;

import io.fabric.sdk.android.Fabric;

public class SnuffyApplication extends Application {
    @Nullable
    private List<SnuffyPage> snuffyPages;
    public SnuffyPage aboutView;
    public String packageTitle;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Enable crash reporting
        Fabric.with(this, new Crashlytics());
        NewRelic.withApplicationToken(BuildConfig.NEW_RELIC_API_KEY).start(this);
    }

    public void sendEmailWithContent(Activity callingActivity, String subjectLine, String msgBody)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
            intent.putExtra(Intent.EXTRA_TEXT, msgBody);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callingActivity.startActivity(Intent.createChooser(intent, getApplicationContext().getString(R.string.choose_your_email_provider)));
        } catch (Exception e)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(callingActivity);
            builder.setMessage(R.string.unable_to_send_the_email)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public File getDocumentsDir()
    {
        File documentsDir = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
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
        if (documentsDir == null)
        {
            documentsDir = getFilesDir();
        }
        return documentsDir;
    }

    @NonNull
    @Deprecated
    public File getResourcesDir() {
        return FileUtils.getResourcesDir(this);
    }

    @Nullable
    public List<SnuffyPage> getSnuffyPages() {
        return snuffyPages;
    }

    public void setSnuffyPages(@Nullable final List<SnuffyPage> pages) {
        this.snuffyPages = pages;
    }
}
