package org.keynote.godtools.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;

public class Settings extends PreferenceActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.getPreferenceManager().setSharedPreferencesName(PREFS_NAME);
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
