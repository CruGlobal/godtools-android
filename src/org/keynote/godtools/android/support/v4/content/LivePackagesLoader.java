package org.keynote.godtools.android.support.v4.content;

import static android.content.Context.MODE_PRIVATE;
import static org.ccci.gto.android.common.db.Expression.bind;
import static org.ccci.gto.android.common.db.Expression.constants;
import static org.keynote.godtools.android.business.GTPackage.STATUS_LIVE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_CODE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.COL_NAME;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.FIELD_CODE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.FIELD_LANGUAGE;
import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.FIELD_STATUS;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.FluentIterable;

import org.ccci.gto.android.common.db.Expression;
import org.ccci.gto.android.common.db.Query;
import org.ccci.gto.android.common.support.v4.content.AsyncTaskSharedPreferencesChangeLoader;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;

import java.util.List;

public class LivePackagesLoader extends AsyncTaskSharedPreferencesChangeLoader<List<GTPackage>> {
    private final DBAdapter mDao;

    // SELECT FROM GTPackage
    // ORDER BY
    //   CASE code WHEN 'everystudent' THEN 1 ELSE 0 END, # ORDER 'everystudent' last
    //   name                                             # and rest by name
    private static final Query<GTPackage> BASE_QUERY = Query.select(GTPackage.class)
            .orderBy("CASE " + COL_CODE + " WHEN 'everystudent' THEN 1 ELSE 0 END, " + COL_NAME);
    private static final Expression BASE_WHERE = FIELD_LANGUAGE.eq(bind()).and(FIELD_STATUS.eq(STATUS_LIVE));

    public LivePackagesLoader(@NonNull final Context context) {
        super(context, context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE));
        mDao = DBAdapter.getInstance(context);

        // watch for the primary language to change
        addPreferenceKey(GTLanguage.KEY_PRIMARY);
    }

    @Override
    public List<GTPackage> loadInBackground() {
        // load packages for the selected language first
        final String language = mPrefs.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        final List<GTPackage> packages = mDao.get(BASE_QUERY.where(BASE_WHERE.args(language)));

        // generate a list of query literals for currently loaded packages
        final String[] available =
                FluentIterable.from(packages).transform(GTPackage.FUNCTION_CODE).toArray(String.class);

        // add on english translations for any missing packages
        final List<GTPackage> englishPackages = mDao.get(
                BASE_QUERY.where(BASE_WHERE.args(ENGLISH_DEFAULT).and(FIELD_CODE.notIn(constants(available)))));
        packages.addAll(englishPackages);

        return packages;
    }
}
