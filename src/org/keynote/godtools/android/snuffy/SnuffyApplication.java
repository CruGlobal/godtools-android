package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;

import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTLanguage;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;
import java.util.Vector;

public class SnuffyApplication extends Application
{

    // Hold pointers to our created objects for the current SnuffyActivity (if any)
    public Vector<SnuffyPage> mPages;
    public SnuffyPage mAboutView;
    public String mPackageTitle;

    public Tracker tracker;

    public Locale mDeviceLocale, mAppLocale;

    @Override
    public void onCreate()
    {
        super.onCreate();

        mDeviceLocale = Locale.getDefault();
        SharedPreferences settings = getSharedPreferences("GodTools", MODE_PRIVATE);
        String primaryLanguageCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        setAppLocale(primaryLanguageCode);

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
        }
        if (documentsDir == null)
        {
            documentsDir = getFilesDir();
        }
        return documentsDir;
    }

    public boolean assetExists(String fileName)
    {
        try
        {
            InputStream is = getAssets().open(fileName);
            is.close();
            return true;
        } catch (Exception e)
        {
            return false;
        }
    }

    public boolean fileExists(String fileName)
    {
        File f = new File(getDocumentsDir() + "/" + fileName);
        return f.exists();
    }

    public boolean languageExistsAsAsset(String packageName, String languageCode)
    {
        String testFileName = "Packages/" + packageName + "/" + languageCode + ".xml";
        return assetExists(testFileName);
    }

    public boolean languageExistsAsFile(String packageName, String languageCode)
    {
        String testFileName = "Packages/" + packageName + "/" + languageCode + ".xml";
        return fileExists(testFileName);
    }

    public Tracker getTracker()
    {
        if (tracker == null)
        {
            return org.keynote.godtools.android.utils.GoogleAnalytics.getTracker(this);
        }

        return tracker;
    }

    public void setAppLocale(String languageCode)
    {

        mAppLocale = new Locale(languageCode);

        Locale.setDefault(mAppLocale);
        Configuration config = new Configuration();
        config.locale = mAppLocale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    public Locale getDeviceLocale()
    {
        return mDeviceLocale;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        mDeviceLocale = newConfig.locale;
        super.onConfigurationChanged(newConfig);

        Locale.setDefault(mAppLocale);
        Configuration config = new Configuration();
        config.locale = mAppLocale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

    }
}
