package org.keynote.godtools.android.service;

import android.content.Context;
import android.content.SharedPreferences;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.KEY_NEW_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.KEY_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.KEY_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.KEY_UPDATE_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

/**
 * Download Service used to work with downloaded content
 */
public class DownloadService
{
    public static void downloadComplete(String langCode, String tag, Context context, SnuffyApplication app)
    {
        DBAdapter mAdapter = DBAdapter.getInstance(context);
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);
        String languageParallel  = settings.getString(GTLanguage.KEY_PARALLEL, EMPTY_STRING);

        GTLanguage gtlPrimary = mAdapter.getGTLanguage(languagePrimary);
        GTLanguage gtlParallel = mAdapter.getGTLanguage(languageParallel);


        if (tag.equalsIgnoreCase(KEY_NEW_LANGUAGE))
        {
            settings.edit().putString(GTLanguage.KEY_PRIMARY, langCode).apply();

            GTLanguage gtl = new GTLanguage(langCode);
            gtl.setDownloaded(true);
            gtl.update(context);

            app.setAppLocale(langCode);
        }
        else if (tag.equalsIgnoreCase(KEY_UPDATE_PRIMARY))
        {
            gtlPrimary.setDownloaded(true);
            gtlPrimary.update(context);

            if (gtlParallel != null && !gtlParallel.isDownloaded())
            {
                BackgroundService.downloadLanguagePack(context, gtlParallel.getLanguageCode(), KEY_UPDATE_PARALLEL);
            }
        }
        else if (tag.equalsIgnoreCase(KEY_UPDATE_PARALLEL))
        {
            gtlParallel.setDownloaded(true);
            gtlParallel.update(context);
        }
        else if (tag.equalsIgnoreCase(KEY_PRIMARY))
        {
            app.setAppLocale(langCode);

            settings.edit().putString(GTLanguage.KEY_PRIMARY, langCode).apply();

            GTLanguage gtl = GTLanguage.getLanguage(context, langCode);
            gtl.setDownloaded(true);
            gtl.update(context);

        }
        else if (tag.equalsIgnoreCase(KEY_PARALLEL))
        {
            settings.edit().putString(GTLanguage.KEY_PARALLEL, langCode).apply();

            GTLanguage gtl = GTLanguage.getLanguage(context, langCode);
            gtl.setDownloaded(true);
            gtl.update(context);

        }
    }
}
