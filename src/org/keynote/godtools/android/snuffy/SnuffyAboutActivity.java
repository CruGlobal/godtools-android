package org.keynote.godtools.android.snuffy;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ScrollView;

import org.keynote.godtools.android.R;

public class SnuffyAboutActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SnuffyPage aboutView = ((SnuffyApplication) getApplication()).aboutView;
        aboutView.mCallingActivity = this; // so AlertDialogs are hosted correctly
        setContentView(R.layout.about_snuffy);
        ScrollView scrollView = (ScrollView) findViewById(R.id.aboutScrollView);
        scrollView.addView(aboutView);
    }

    @Override
    protected void onDestroy()
    {

        SnuffyPage aboutView = ((SnuffyApplication) getApplication()).aboutView;
        ViewGroup parent = (ViewGroup) aboutView.getParent();
        parent.removeView(aboutView);

        super.onDestroy();
    }
}
