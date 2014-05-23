package org.keynote.godtools.android.snuffy;

import java.io.File;
import java.io.InputStream;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.GoogleAnalyticsConfig;

public class SnuffyApplication extends Application {
	
	// Hold pointers to our created objects for the current SnuffyActivity (if any)
	public Vector<SnuffyPage>		mPages;
	public SnuffyPage				mAboutView;
	public String					mPackageTitle;

	public Tracker tracker;

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void sendEmailWithContent(Activity callingActivity, String subjectLine, String msgBody) {
		try {
	        Intent intent = new Intent(Intent.ACTION_SENDTO); 
	        intent.setData(Uri.parse("mailto:")); 
	        intent.putExtra(Intent.EXTRA_SUBJECT, subjectLine); 
	        intent.putExtra(Intent.EXTRA_TEXT   , msgBody);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        callingActivity.startActivity(Intent.createChooser(intent, getApplicationContext().getString(R.string.choose_your_email_provider)));
		} catch (Exception e){
			AlertDialog.Builder builder = new AlertDialog.Builder(callingActivity);
			builder.setMessage(R.string.unable_to_send_the_email)
					.setCancelable(false)
					.setPositiveButton(R.string.ok, null);
			AlertDialog alert = builder.create();
			alert.show();
			return;			
		}
	}
	
	public File getDocumentsDir() {
		File documentsDir = null;
		if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
			documentsDir = getExternalFilesDir(null);
		}
		if (documentsDir == null) {
			documentsDir = getFilesDir();
		}
		return documentsDir;			
	}
	
	public boolean assetExists(String fileName) {
		try {
			InputStream is = getAssets().open(fileName);
			is.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	public boolean fileExists(String fileName) {
		File f = new File (getDocumentsDir() + "/" + fileName);
		return f.exists();
	}
	
	public boolean languageExistsAsAsset(String packageName, String languageCode) {
    	String testFileName   = "Packages/" + packageName + "/" + languageCode + ".xml";
    	return assetExists(testFileName);		
	}

	public boolean languageExistsAsFile(String packageName, String languageCode) {
    	String testFileName   = "Packages/" + packageName + "/" + languageCode + ".xml";
    	return fileExists(testFileName);		
	}
	
	public boolean languageExists(String packageName, String languageCode) {
    	String testFileName   = "Packages/" + packageName + "/" + languageCode + ".xml";
    	return assetExists(testFileName) || fileExists(testFileName);
	}

	public Tracker getTracker()
	{
		if(tracker == null)
		{
			return org.keynote.godtools.android.utils.GoogleAnalytics.getTracker(this);
		}

		return tracker;
	}
	
}
