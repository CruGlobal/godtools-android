package org.keynote.godtools.android;
 
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;
 
public class About extends Activity {
 
	public static final String LOGTAG = "About";
	public static final String FLURRYTAG = "Settings"; // we are now calling this from the Settings cmd and button
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		// Initialize the layout
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.about);
 
		// display the application version
		TextView versionVeiw = (TextView) this.findViewById(R.id.app_version);
		try {
			String version = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			versionVeiw.setText(version);
		} catch (NameNotFoundException e) {}
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