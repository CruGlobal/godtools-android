package org.keynote.godtools.android.service;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.InputStream;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.KEY_NEW_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PRIMARY;

/**
 * Created by matthewfrederick on 5/11/15.
 */
public class UpdatePackageListTask
{
    private static final String TAG = "UpdatePackageListTask";

    public static void run(InputStream is, DBAdapter adapter, boolean firstLaunch, SnuffyApplication app,
                           String languagePrimary, String languageParallel, Context context)
    {
        Log.i(TAG, "Update Package List Backgroupd");

        List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

        Log.i(TAG, "List Size: " + languageList.size());

        adapter.open();

        for (GTLanguage gtl : languageList)
        {
            // check if language is already in the db
            GTLanguage dbLanguage = adapter.getGTLanguage(gtl.getLanguageCode());
            if (dbLanguage == null)
            {
                adapter.insertGTLanguage(gtl);
            }
            else
            {
                // don't forget that a previously downloaded language was already downloaded.
                gtl.setDownloaded(dbLanguage.isDownloaded());
                adapter.updateGTLanguage(gtl);
            }

            dbLanguage = adapter.getGTLanguage(gtl.getLanguageCode());
            for (GTPackage gtp : gtl.getPackages())
            {
                // check if a new package is available for download or an existing package has been updated
                GTPackage dbPackage = adapter.getGTPackage(gtp.getCode(), gtp.getLanguage(), gtp.getStatus());
                if (dbPackage == null || (gtp.getVersion() > dbPackage.getVersion()))
                {
                    dbLanguage.setDownloaded(false);
                }
            }

            adapter.updateGTLanguage(dbLanguage);
        }

        GTLanguage gtlPrimary = adapter.getGTLanguage(languagePrimary);
        GTLanguage gtlParallel = adapter.getGTLanguage(languageParallel);

        if (firstLaunch)
        {
            if (shouldUpdateLanguageSettings(app, languagePrimary, context))
            {
                // download resources for the phone's language
                String languagePhone = app.getDeviceLocale().getLanguage();
                BackgroundService.downloadLanguagePack(context, languagePhone, KEY_NEW_LANGUAGE);
            }
            else if (!gtlPrimary.isDownloaded())
            {
                BackgroundService.downloadLanguagePack(context, languagePrimary, KEY_UPDATE_PRIMARY);
            }
        }
        else
        {
            Log.i(TAG, "Not First Launch");

            if (!gtlPrimary.isDownloaded())
            {
                BackgroundService.downloadLanguagePack(context, languagePrimary, KEY_UPDATE_PRIMARY);
            }
            else
            {
                // update the resources for the parallel language
                if (gtlParallel != null && !gtlParallel.isDownloaded())
                {
                    BackgroundService.downloadLanguagePack(context, gtlParallel.getLanguageCode(), KEY_UPDATE_PARALLEL);
                }
                else
                {
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    broadcastManager.sendBroadcast(BroadcastUtil.stopBroadcast(Type.DOWNLOAD_TASK));
                }
            }
        }
        adapter.close();
    }

    private static boolean shouldUpdateLanguageSettings(SnuffyApplication app, String languagePrimary, Context context)
    {
        // check first if the we support the phones language
        String languagePhone = app.getDeviceLocale().getLanguage();
        GTLanguage gtlPhone = GTLanguage.getLanguage(context, languagePhone);
        return gtlPhone != null && !languagePrimary.equalsIgnoreCase(languagePhone);
    }
}
