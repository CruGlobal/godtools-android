package org.keynote.godtools.android.service;

import android.util.Log;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;

import java.util.List;

public class UpdatePackageListTask
{
    private static final String TAG = "UpdatePackageListTask";

//    public static void run(InputStream is, DBAdapter adapter)
//    {
//        Log.i(TAG, "Update Package List Backgroupd");
//
//        List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);
//
//        run(languageList, adapter);
//    }

    public static void run(List<GTLanguage> languageList, DBAdapter adapter)
    {
        Log.i(TAG, "List Size: " + languageList.size());

        for (GTLanguage languageFromMetaDownload : languageList)
        {
            adapter.updateOrInsert(languageFromMetaDownload, GTLanguageTable.COL_NAME/*, GTLanguageTable.COL_DRAFT*/);

            GTLanguage languageRetrievedFromDatabase = adapter.refresh(languageFromMetaDownload);
            if(languageRetrievedFromDatabase != null) {
                for (GTPackage packageFromMetaDownload : languageFromMetaDownload.getPackages()) {
                    // check if a newnew package is available for download or an existing package has been updated
                    final GTPackage packageRetrievedFromDatabase = adapter.refresh(packageFromMetaDownload);
                    if (packageRetrievedFromDatabase == null ||
                            packageRetrievedFromDatabase.compareVersionTo(packageFromMetaDownload) < 0) {
                        languageRetrievedFromDatabase.setDownloaded(false);
                        break;
                    }
                }

                adapter.update(languageRetrievedFromDatabase, GTLanguageTable.COL_DOWNLOADED);
            }
        }
    }
}
