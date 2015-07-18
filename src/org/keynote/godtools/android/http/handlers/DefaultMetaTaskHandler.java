package org.keynote.godtools.android.http.handlers;

import android.content.Context;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.UpdatePackageListTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.util.List;

/**
 * Created by ryancarlson on 7/18/15.
 */
public class DefaultMetaTaskHandler implements MetaTask.MetaTaskHandler
{

    private DBAdapter adapter;
    private boolean isFirstLaunch;
    private SnuffyApplication application;
    private String languagePrimary;
    private String languageParallel;
    private Context context;

    public DefaultMetaTaskHandler(DBAdapter adapter, boolean isFirstLaunch, SnuffyApplication application, String languagePrimary, String languageParallel, Context context)
    {
        this.adapter = adapter;
        this.isFirstLaunch = isFirstLaunch;
        this.application = application;
        this.languagePrimary = languagePrimary;
        this.languageParallel = languageParallel;
        this.context = context;
    }

    @Override
    public void metaTaskComplete(List<GTLanguage> languageList, String tag)
    {

    }

    @Override
    public void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode)
    {

    }
}
