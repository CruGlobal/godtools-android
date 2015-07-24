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
                if (dbPackage == null || newerVersionExists(gtp.getVersion(), dbPackage.getVersion()))
                {
                    dbLanguage.setDownloaded(false);
                    break;
                }
            }

            adapter.updateGTLanguage(dbLanguage);
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
