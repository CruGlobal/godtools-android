package org.keynote.godtools.android.service;

import android.util.Log;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class UpdatePackageListTask
{
    private static final String TAG = "UpdatePackageListTask";

    public static void run(InputStream is, DBAdapter adapter)
    {
        Log.i(TAG, "Update Package List Backgroupd");

        List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

        run(languageList, adapter);
    }

    public static void run(List<GTLanguage> languageList, DBAdapter adapter)
    {
        Log.i(TAG, "List Size: " + languageList.size());

        adapter.open();

        for (GTLanguage languageFromMetaDownload : languageList)
        {
            // check if language is already in the db
            GTLanguage languageRetrievedFromDatabase = adapter.getGTLanguage(languageFromMetaDownload.getLanguageCode());
            if (languageRetrievedFromDatabase == null)
            {
                adapter.insertGTLanguage(languageFromMetaDownload);
            }
            else
            {
                // don't forget that a previously downloaded language was already downloaded.
                languageFromMetaDownload.setDownloaded(languageRetrievedFromDatabase.isDownloaded());
                adapter.updateGTLanguage(languageFromMetaDownload);
            }

            languageRetrievedFromDatabase = adapter.getGTLanguage(languageFromMetaDownload.getLanguageCode());
            for (GTPackage packageFromMetaDownload : languageFromMetaDownload.getPackages())
            {
                // check if a new package is available for download or an existing package has been updated
                GTPackage packageRetrievedFromDatabase = adapter.getGTPackage(packageFromMetaDownload.getCode(), packageFromMetaDownload.getLanguage(), packageFromMetaDownload.getStatus());
                if (packageRetrievedFromDatabase == null || newerVersionExists(packageFromMetaDownload.getVersion(), packageRetrievedFromDatabase.getVersion()))
                {
                    languageRetrievedFromDatabase.setDownloaded(false);
                    break;
                }
            }

            adapter.updateGTLanguage(languageRetrievedFromDatabase);
        }
    }

    /*
        For an explanation of how the decimal number portion is extracted to compare the minor version numbers,
        see: http://stackoverflow.com/questions/10383392/extract-number-decimal-in-bigdecimal
     */
    private static boolean newerVersionExists(double remoteVersion, double localVersion)
    {
        // convert to string first to avoid floating point issues
        BigDecimal wrappedRemoteVersion = new BigDecimal(String.valueOf(remoteVersion));
        BigDecimal wrappedLocalVersion = new BigDecimal(String.valueOf(localVersion));

        // check the major version number portion.  if removeVersion is higher, then return true.
        // otherwise continue on to minor version number portion
        if(wrappedRemoteVersion.intValue() > wrappedLocalVersion.intValue()) return true;

        int integerRemoteMinorVersion = wrappedRemoteVersion.subtract(wrappedRemoteVersion.setScale(0, RoundingMode.FLOOR)).movePointRight(wrappedRemoteVersion.scale()).intValue();
        int integerLocalMinorVersion = wrappedLocalVersion.subtract(wrappedLocalVersion.setScale(0, RoundingMode.FLOOR)).movePointRight(wrappedLocalVersion.scale()).intValue();

        return integerRemoteMinorVersion > integerLocalMinorVersion;
    }
}
