package org.keynote.godtools.android.support.v4.content;

import static android.content.Context.MODE_PRIVATE;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

import android.content.Context;
import android.support.annotation.NonNull;

import org.ccci.gto.android.common.support.v4.content.AsyncTaskSharedPreferencesChangeLoader;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;

import java.util.List;

public class LivePackagesLoader extends AsyncTaskSharedPreferencesChangeLoader<List<GTPackage>> {
    public LivePackagesLoader(@NonNull final Context context) {
        super(context, context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        addPreferenceKey(GTLanguage.KEY_PRIMARY);
    }

    @Override
    public List<GTPackage> loadInBackground() {
        // try loading packages for the selected primary language
        final String language = mPrefs.getString(GTLanguage.KEY_PRIMARY, "");
        final List<GTPackage> packages = GTPackage.getLivePackages(getContext(), language);
        if (packages != null && !packages.isEmpty()) {
            return packages;
        }

        // we didn't find any packages, default the language to english and retry
        mPrefs.edit().putString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT).apply();
        return GTPackage.getLivePackages(getContext(), ENGLISH_DEFAULT);
    }
}
