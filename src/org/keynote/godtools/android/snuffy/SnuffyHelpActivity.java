package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.os.Bundle;

import org.keynote.godtools.android.R;


public class SnuffyHelpActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Initialize the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snuffy_help);

        String packageTitle = getIntent().getStringExtra("PackageTitle");
        String windowTitle = getString(R.string.snuffy_help_title);
        windowTitle = windowTitle.replace("%1", packageTitle);
        setTitle(windowTitle);
    }
}