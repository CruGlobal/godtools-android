package org.keynote.godtools.android.http.handlers;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.http.MetaTask;

import java.util.List;

/**
 * Created by ryancarlson on 7/18/15.
 */
public class NoOpMetaTaskHandler implements MetaTask.MetaTaskHandler
{
    @Override
    public void metaTaskComplete(List<GTLanguage> languageList, String tag)
    {

    }

    @Override
    public void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode)
    {

    }
}
