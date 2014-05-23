package org.keynote.godtools.android.snuffy;

//import org.keynote.godtools.android.utils.FlurryAPI;

import org.keynote.godtools.android.R;
//import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.os.Bundle;
 
public class SnuffyHelpActivity extends Activity {
 
	public static final String LOGTAG = "SnuffyHelp";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Initialize the layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snuffy_help);
		
		String packageTitle = getIntent().getStringExtra("PackageTitle");
		String windowTitle = getString(R.string.snuffy_help_title);
		windowTitle = windowTitle.replace("%1", packageTitle);
		setTitle(windowTitle);		
		
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