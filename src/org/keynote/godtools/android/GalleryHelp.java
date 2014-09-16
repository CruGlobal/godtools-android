package org.keynote.godtools.android;

//import org.keynote.godtools.android.utils.FlurryAPI;

//import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.os.Bundle;

public class GalleryHelp extends Activity
{

    public static final String LOGTAG = "GalleryHelp";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // Initialize the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_help);

        //FlurryAgent.onEvent(FlurryAPI.FlurryPrefix + LOGTAG);
        //FlurryAgent.onPageView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //FlurryAPI.onStartSession(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        //FlurryAgent.onEndSession(this);
    }
}