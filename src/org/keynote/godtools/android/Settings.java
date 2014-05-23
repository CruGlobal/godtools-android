package org.keynote.godtools.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity {

	public static final String LOGTAG = "Settings";
	public static final String PREFNAME = "GodTools";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getPreferenceManager().setSharedPreferencesName(PREFNAME);
        addPreferencesFromResource(R.xml.settings);
    }
    
    @Override
    public void onStart()
    {
       super.onStart();
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
    }

}
