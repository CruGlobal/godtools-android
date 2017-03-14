package org.keynote.godtools.android.service;

import android.support.annotation.NonNull;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.GTLanguageTable;

import java.util.List;

public class UpdatePackageListTask {
    public static void run(@NonNull final List<GTLanguage> languages, @NonNull final DBAdapter adapter) {
        for (final GTLanguage language : languages) {
            adapter.updateOrInsert(language, GTLanguageTable.COL_NAME);

            for (final GTPackage newPackage : language.getPackages()) {
                newPackage.setLanguage(language.getLanguageCode());

                // check if a new new package is available for download or an existing package has been updated
                final GTPackage currentPackage = adapter.refresh(newPackage);
                if (currentPackage == null || currentPackage.compareVersionTo(newPackage) < 0) {
                    language.setDownloaded(false);
                    adapter.update(language, GTLanguageTable.COL_DOWNLOADED);
                    break;
                }
            }
        }

    }
}
