package org.keynote.godtools.android;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.fragments.AlertDialogFragment;
import org.keynote.godtools.android.fragments.LanguageDialogFragment;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;


public class Main extends BaseActionBarActivity implements LanguageDialogFragment.OnLanguageChangedListener, PackageListFragment.OnPackageSelectedListener, DownloadTask.DownloadTaskHandler, MetaTask.MetaTaskHandler
{
	private static final String TAG = "Main";
    private static final String TAG_LIST = "PackageList";
    private static final String TAG_DIALOG_LANGUAGE = "LanguageDialog";
    private static final int REQUEST_SETTINGS = 1001;
	private static final String PREFS_NAME = "GodTools";

	public static final int		REFERENCE_DEVICE_HEIGHT = 960;	// pixels on iPhone w/retina - including title bar
	public static final int		REFERENCE_DEVICE_WIDTH  = 640;	// pixels on iPhone w/retina - full width

	private int	mPageLeft;
	private int	mPageTop;
	private int	mPageWidth;
	private int	mPageHeight;
	private boolean	mSetupNeeded;
    private String languagePrimary;
    private List<GTPackage> packageList;
    PackageListFragment packageFrag;
    View vLoading;
    ImageButton ibRefresh;
    TextView tvTask;

    boolean isDownloading;
    String authorization;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);

        vLoading = findViewById(R.id.contLoading);
        tvTask = (TextView) findViewById(R.id.tvTask);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
		Log.i("matt", "Primary language is " + languagePrimary);
        authorization = getString(R.string.key_authorization_generic);

        packageList = getPackageList(); // get the packages for the primary language

        FragmentManager fm = getSupportFragmentManager();
        packageFrag = (PackageListFragment) fm.findFragmentByTag(TAG_LIST);
        if (packageFrag == null)
        {
            packageFrag = PackageListFragment.newInstance(languagePrimary, packageList);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.contList, packageFrag, TAG_LIST);
            ft.commit();
        }
        
        // Make the Settings button highlight when pressed (without defining a separate image)
        ImageButton button = (ImageButton) findViewById(R.id.homescreen_settings_button);
        button.setOnTouchListener(new OnTouchListener() {
        	@Override
        	public boolean onTouch(View arg0, MotionEvent me) {
        		ImageButton button = (ImageButton)arg0;
        		Drawable d = button.getBackground();
        		PorterDuffColorFilter grayFilter =  
        			    new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP); 

        		if (me.getAction() == MotionEvent.ACTION_DOWN) {
        			d.setColorFilter(grayFilter);
        			button.invalidate();
            		return false;
        		}
        		else if (me.getAction() == MotionEvent.ACTION_UP) {
        			d.setColorFilter(null);
        			button.invalidate();
            		return false;
        		}
        		else
        			return false;
        	}         
        });        

		mSetupNeeded = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case RESULT_CHANGED_PRIMARY: {

                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(data.getStringExtra("primaryCode"));

                languagePrimary = data.getStringExtra("primaryCode");
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, packageList);

                break;
            }
            case RESULT_DOWNLOAD_PRIMARY: {

                // start the download
                String code = data.getStringExtra("primaryCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        authorization,
                        this);

                break;
            }
            case RESULT_DOWNLOAD_PARALLEL: {

                // refresh the list if the primary language was changed
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    languagePrimary = primaryCode;
                    packageList = getPackageList();
                    packageFrag.refreshList(languagePrimary, packageList);
                }

                String code = data.getStringExtra("parallelCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "parallel",
                        authorization,
                        this);
                break;
            }
            case RESULT_PREVIEW_MODE_ENABLED: {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(true);
                ibRefresh = (ImageButton) findViewById(R.id.ibRefresh);

                // refresh the list
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                languagePrimary = primaryCode;
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, packageList);

                showLoading("Downloading drafts...");
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, primaryCode, "draft_primary", this);

                Toast.makeText(Main.this, "Translator preview mode is enabled", Toast.LENGTH_LONG).show();
                break;
            }
            case RESULT_PREVIEW_MODE_DISABLED: {
                ActionBar actionBar = getSupportActionBar();
                actionBar.setDisplayShowCustomEnabled(false);
                ibRefresh = null;

                // refresh the list
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode)) {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                languagePrimary = primaryCode;
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, packageList);

                Toast.makeText(Main.this, "Translator preview mode is disabled", Toast.LENGTH_LONG).show();
                break;
            }
        }

    }
  

    @Override
    public void onStart()
    {
       super.onStart();
       Log.d(TAG, "onStart");
    }
    
    @Override
    public void onStop()
    {
       super.onStop();
    }

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        // E.g.
        //ed.putInt("currPage", mPagerCurrentItem);
        //ed.putString("currLanguageCode", getLanguage());
        ed.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Setup is only done on first resume, 
		// else we resize the screen elements smaller and smaller each time.
		if (mSetupNeeded) {
			doSetup(1000); 	// 1 second delay required to make sure activity fully created 
							// - is there something we can test for that is better than a fixed timeout?
		}
	}
	
	private void doSetup(int delay) {
		new Handler().postDelayed(new Runnable() {
            public void run() {
                resizeTheApp();
                createTheHomeScreen();
                showTheHomeScreen();
                mSetupNeeded = false;
            }
        }, delay);  // delay can be required to make sure activity fully created - is there something we can test for that is better than a fixed timeout?
		
	}
	
	private void resizeTheApp() {
		Rect r = new Rect();
	    Window w = getWindow();
	    w.getDecorView().getWindowVisibleDisplayFrame(r);
	    // we want to ignore the status bar 
	    // since we have asked for it not to be displayed 
	    // but getWindowVisibleDisplayFrame is still reporting as if it was there
	    r.top = 0;
	    int width  = r.width();
	    int height = r.height();
	    int left   = r.left;
	    int top    = r.top;

 	    double aspectRatioTarget = (double)Main.REFERENCE_DEVICE_WIDTH / (double)Main.REFERENCE_DEVICE_HEIGHT;
	    double aspectRatio       = (double)r.width()                   / (double)r.height();
	    if (aspectRatio > aspectRatioTarget) {
	    	height = r.height();
	    	width  = (int)Math.round(height * aspectRatioTarget);	    	
	    }
	    else {
	    	width  = r.width();
	    	height = (int)Math.round(width  / aspectRatioTarget);	    		    	
	    }
	    
    	// Update layout to set the size we have decided to use instead of FILL_PARENT
        View container = findViewById(R.id.homescreen_container);
        AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)container.getLayoutParams();
        lp.width = width;
        lp.height = height;
        // and also center it
        left = r.left + (r.width () - width )/2;
        top  = 0      + (r.height() - height)/2; // r is from top of screen , lp is from bottom of status bar 
        // (also note that iphone status bar is 20, Android in small layout is still 25. So we lose 5 pixels, 23 at top, 2 at bottom!)
        
        lp.x = left;
        lp.y = top;
        container.setLayoutParams(lp);
	        
	    mPageLeft	= left;
	    mPageTop	= top;
	    mPageWidth	= width;
	    mPageHeight	= height;
	}
	
	private void createTheHomeScreen() {
		Context	context = getApplicationContext();
// 		Bitmap bm = context.getResources().getDrawable(R.drawable.homescreen_godtools_logo); //   getBitmapFromAssetOrFile(context, backgroundImage);
//		if (bm != null) {
//    		ImageView iv = new ImageView(context);
//        	iv.setLayoutParams(new SnuffyLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0, 0));
//        	iv.setImageBitmap(bm);
//    		iv.setScaleType(ImageView.ScaleType.FIT_XY);
//    		currPage.addView(iv);
//		}

        ViewGroup container = (ViewGroup) findViewById(R.id.homescreen_container);
        // assume container only has direct (not nested) children
        // and each has absolute layout
        int n = container.getChildCount();
        for (int i=0; i < n ; i++) {
        	View v = container.getChildAt(i);
            AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)v.getLayoutParams();
            lp.x = getScaledXValue(lp.x);
            lp.y = getScaledYValue(lp.y);
            lp.width  = getScaledXValue(lp.width);
            lp.height = getScaledYValue(lp.height);
            v.setLayoutParams(lp);
            if (v.getClass() == TextView.class) {
            	TextView tv = (TextView) v;
            	tv.setTextSize(getScaledTextSize(tv.getTextSize()));
            }
        }
	}
	
	private void showTheHomeScreen() {
		// Now that it is resized - show it
		// TODO: Can we fade it in?
		ViewGroup container = (ViewGroup) findViewById(R.id.homescreen_container);
		container.setVisibility(View.VISIBLE);
		
	}
	
	
	private float getScaledTextSize(float textSize) {
		final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
		return (textSize * (float)mPageHeight / (float)REFERENCE_DEVICE_HEIGHT )/scale;		
	}

	private int getScaledXValue(int x) {
		return (int)Math.round((double)(x * mPageWidth) / (double)REFERENCE_DEVICE_WIDTH);
	}
	private int getScaledYValue(int y) {
		return (int)Math.round((double)(y * mPageHeight) / (double)REFERENCE_DEVICE_HEIGHT);
	}

    private void showLoading(String msg) {
        isDownloading = true;
        supportInvalidateOptionsMenu();
        tvTask.setText(msg);
        vLoading.setVisibility(View.VISIBLE);
        packageFrag.disable();

        if (ibRefresh != null) {
            ibRefresh.setEnabled(false);
        }
    }

    @Override
    public void onLanguageChanged(String name, String code) {

        GTLanguage gtLanguage = GTLanguage.getLanguage(Main.this, code);
        if (gtLanguage.isDownloaded()) {
            languagePrimary = gtLanguage.getLanguageCode();
            packageList = getPackageList();
            packageFrag.refreshList(languagePrimary, packageList);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, code);

            String parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
            if (code.equalsIgnoreCase(parallelLanguage))
                editor.putString(GTLanguage.KEY_PARALLEL, "");

            editor.commit();

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(code);


        } else {

            if (Device.isConnected(Main.this)) {
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        authorization,
                        this);
            } else {
                // TODO: show dialog, Internet connection is required to download the resources
                Toast.makeText(this, "Unable to download resources. Internet connection unavailable.", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {

        if (tag.equalsIgnoreCase("primary")) {

            languagePrimary = langCode;

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(langCode);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(Main.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(Main.this);

            if (isTranslatorModeEnabled()) {
                // check for draft_primary
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, langCode, "draft_primary", this);

            } else {
                packageList = getPackageList();
                packageFrag.refreshList(langCode, packageList);
                hideLoading();
            }

        } else if (tag.equalsIgnoreCase("parallel")) {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(Main.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(Main.this);

            if (isTranslatorModeEnabled()) {
                // check for draft_parallel
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.getListOfDrafts(authorization, langCode, "draft_parallel", this);

            } else {
                hideLoading();
            }

        } else if (tag.equalsIgnoreCase("draft")) {

            Toast.makeText(Main.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            hideLoading();

        } else if (tag.equalsIgnoreCase("draft_primary")) {

            languagePrimary = langCode;
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            hideLoading();

        } else if (tag.equalsIgnoreCase("draft_parallel")) {

            hideLoading();

        }

    }

    private List<GTPackage> getPackageList() {
        if (isTranslatorModeEnabled()) {
            return GTPackage.getPackageByLanguage(Main.this, languagePrimary);
        } else {
            return GTPackage.getLivePackages(Main.this, languagePrimary);
        }
    }

    private boolean isTranslatorModeEnabled() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("TranslatorMode", false);
    }

    @Override
    public void onPackageSelected(GTPackage gtPackage)
	{
		if (gtPackage.getCode().equalsIgnoreCase("everystudent"))
		{
			Intent intent = new Intent(this, EveryStudent.class);
			intent.putExtra("PackageName", gtPackage.getCode());
			addPageFrameToIntent(intent);
			startActivity(intent);
		}
		else
		{
			Intent intent = new Intent(this, SnuffyPWActivity.class);
			intent.putExtra("PackageName", gtPackage.getCode());
			intent.putExtra("LanguageCode", gtPackage.getLanguage());
			intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
			intent.putExtra("Status", gtPackage.getStatus());
			addPageFrameToIntent(intent);
			startActivity(intent);
		}

    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag) {
        // process the input stream
        new UpdateDraftListTask().execute(is, langCode, tag);
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag) {

        if (tag.equalsIgnoreCase("draft") || tag.equalsIgnoreCase("draft_primary")) {
            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
        }

        hideLoading();
        Toast.makeText(Main.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag) {

        if (tag.equalsIgnoreCase("draft")) {

            Toast.makeText(Main.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();

        } else if (tag.equalsIgnoreCase("draft_primary")) {

            packageList = getPackageList();
            packageFrag.refreshList(langCode, packageList);
            Toast.makeText(Main.this, "Failed to download drafts", Toast.LENGTH_SHORT).show();

        } else if (tag.equalsIgnoreCase("draft_parallel")) {

            // do nothing

        } else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel")) {

            Toast.makeText(Main.this, "Failed to download resources", Toast.LENGTH_SHORT).show();

        }

        hideLoading();
    }

    private class UpdateDraftListTask extends AsyncTask<Object, Void, Boolean> {
        boolean mNewDraftsAvailable;
        String tag, langCode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mNewDraftsAvailable = false;
        }

        @Override
        protected Boolean doInBackground(Object... params) {

            InputStream is = (InputStream) params[0];
            langCode = params[1].toString();
            tag = params[2].toString();

            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

            GTLanguage language = languageList.get(0);
            List<GTPackage> packagesDraft = language.getPackages();

            return packagesDraft.size() != 0;
        }

        @Override
        protected void onPostExecute(Boolean shouldDownload) {
            super.onPostExecute(shouldDownload);

            if (shouldDownload) {

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String authorization = settings.getString("authorization", getString(R.string.key_authorization_generic));
                GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(), authorization, langCode, tag, Main.this);


            } else {

                if (tag.equalsIgnoreCase("draft")) {

                    FragmentManager fm = getSupportFragmentManager();
                    DialogFragment frag = (DialogFragment) fm.findFragmentByTag("alert_dialog");
                    if (frag == null) {
                        Locale primary = new Locale(langCode);
                        frag = AlertDialogFragment.newInstance("Drafts", String.format("No drafts available for %s", primary.getDisplayName()));
                        frag.setCancelable(false);
                        frag.show(fm, "alert_dialog");
                    }

                } else if (tag.equalsIgnoreCase("draft_primary")) {

                    languagePrimary = langCode;
                    packageList = getPackageList();
                    packageFrag.refreshList(langCode, packageList);

                } else if (tag.equalsIgnoreCase("draft_parallel")) {
                    // do nothing
                }

                hideLoading();
            }
        }
    }

    private void hideLoading() {
        isDownloading = false;
        supportInvalidateOptionsMenu();
        tvTask.setText("");
        vLoading.setVisibility(View.GONE);
        packageFrag.enable();

        if (ibRefresh != null) {
            ibRefresh.setEnabled(true);
        }
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.CMD_SETTINGS:
            	onCmd_settings(null);
                return true;
               
            case R.id.CMD_QUIT:
            	quit();
            	return true;
        }
        
        return false;
    }
 	

	private void addPageFrameToIntent (Intent intent) {
       	intent.putExtra("PageLeft"  , mPageLeft);
    	intent.putExtra("PageTop"   , mPageTop);
    	intent.putExtra("PageWidth" , mPageWidth);
    	intent.putExtra("PageHeight", mPageHeight);		
	}
	
	public void onCmd_settings(View view) {
	   	Intent intent = new Intent(this, SettingsPW.class);
    	startActivityForResult(intent, REQUEST_SETTINGS);
	}
 
    private void quit()
    {
       super.onDestroy();
       this.finish();
    }
	
}