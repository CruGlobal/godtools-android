package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.os.Bundle;

import org.keynote.godtools.android.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import static org.keynote.godtools.android.utils.Constants.PACKAGE_NAME;

public class SnuffyHelpActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Initialize the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snuffy_help);

        String packageTitle = RenderSingleton.getInstance().getGDocument().packagename.content;
        String windowTitle = getString(R.string.snuffy_help_title);
        windowTitle = windowTitle.replace(PACKAGE_NAME, packageTitle);
        setTitle(windowTitle);
    }
}