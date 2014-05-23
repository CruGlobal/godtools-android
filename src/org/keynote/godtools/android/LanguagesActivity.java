package org.keynote.godtools.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.snuffy.SnuffyActivity;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class LanguagesActivity extends ListActivity {

	// Note that these coords are those used in the language_menu.XML file.
	// The identical specs in Main.java in the GodTools app are based on
	// an iPhone4S with retina display (960x640), with the status bar removed.
	// PackageReader.java uses specs based on an old iPhone model (480x320)
	
	//public static final int		REFERENCE_DEVICE_HEIGHT = 920;	// pixels on iPhone w/retina - not including title bar
	public static final int		REFERENCE_DEVICE_HEIGHT = 960;	// pixels on iPhone w/retina - including title bar
	public static final int		REFERENCE_DEVICE_WIDTH  = 640;	// pixels on iPhone w/retina - full width
	
	private String 				mPackageName;
	private int					mPageLeft;
	private int					mPageTop;
	private int					mPageWidth;
	private int					mPageHeight;
	private boolean				mSetupRequired = true;
	private List<Map<String, String>> mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.language_menu);
		
		// TODO: Resize this on first entry - using screen size params detected in Main.java
				
		// see also : http://stackoverflow.com/questions/6852876/android-about-listview-and-simpleadapter
		// see also: http://android-developers.blogspot.com.au/2009/02/android-layout-tricks-1.html
		
		// the from array specifies which keys from the map
		// we want to view in our ListView
		String[] from = { "label"};
		
		// the to array specifies the views from the xml layout
		// on which we want to display the values defined in the from array
		int[] to = { R.id.list1Text};
		
		mPackageName  	= getIntent().getStringExtra("PackageName");
		mPageLeft		= getIntent().getIntExtra("PageLeft", 0);
		mPageTop		= getIntent().getIntExtra("PageTop" , 0);
		mPageWidth		= getIntent().getIntExtra("PageWidth" , 320); // set defaults but they will not be used
		mPageHeight		= getIntent().getIntExtra("PageHeight", 480); // caller will always determine these and pass them in
		
		// TODO: consider case where device rotated - this code may need to move

		// TODO: Can we supply this info as XML file?
		mList = new ArrayList<Map<String, String>>();
		HashMap<String, String> map = null;
		if (mPackageName.equalsIgnoreCase("kgp")) {
			map = new HashMap<String, String>();
			map.put("label", "English");
			map.put("code" , "en");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Estonian");
			map.put("code" , "et");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Spanish, Latin America");
			map.put("code" , "es");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Tagalog");
			map.put("code" , "tgl");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Thai");
			map.put("code" , "th");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Ukrainian");
			map.put("code" , "uk");
			mList.add(map);
		}
		if (mPackageName.equalsIgnoreCase("satisfied")) {
			map = new HashMap<String, String>();
			map.put("label", "English");
			map.put("code" , "en");
			mList.add(map);
		}
		if (mPackageName.equalsIgnoreCase("fourlaws")) {
			map = new HashMap<String, String>();
			map.put("label", "Albanian");
			map.put("code" , "sq");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Amharic (Ethiopia)");
			map.put("code" , "am-ET");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Chinese Simplified");
			map.put("code" , "zh");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Czech");
			map.put("code" , "cs");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "English");
			map.put("code" , "en");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Korean");
			map.put("code" , "ko");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Lithuanian");
			map.put("code" , "lt");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Polish");
			map.put("code" , "pl");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Russian");
			map.put("code" , "ru");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Spanish, Latin America");
			map.put("code" , "es");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Tagalog");
			map.put("code" , "tgl");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Thai");
			map.put("code" , "th");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Turkish");
			map.put("code" , "tr");
			mList.add(map);
			map = new HashMap<String, String>();
			map.put("label", "Ukrainian");
			map.put("code" , "uk");
			mList.add(map);
		}
		if (mPackageName.equalsIgnoreCase("everystudent")) {
			map = new HashMap<String, String>();
			map.put("label", "English");
			map.put("code" , "en_classic");
			mList.add(map);
		}
		int   listItemHeight = getScaledYValue(88);
		float textItemHeight = getScaledTextSize(30);
		LanguageListAdapter adapter = new LanguageListAdapter(
				this, mList, R.layout.language_menu_list_item, from, to,
				new int[] { R.drawable.cell_background_dark_selector, R.drawable.cell_background_light_selector},
				listItemHeight, textItemHeight);
		setListAdapter(adapter);
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

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mSetupRequired == false) {
			// package processing has been done - this is resume after pause - not after create
			return;
		}
		mSetupRequired = false;
		
		doSetup(100); // used to be 1 second delay required to make sure activity fully created 
		// - is there something we can test for that is better than a fixed timeout?
		// We reduce this now to 100 msec since we are not measuring the device size here
		// since that is done in GodTools which calls us and passes the dimensions in.
		
	}
	
	private void doSetup(int delay) {
		new Handler().postDelayed(new Runnable() {
			public void run() {				
				resizeTheActivity();
				resizeTheActivityContents();
				showTheActivityContents();
			}
		}, delay);  // delay can be required to make sure activity fully created - is there something we can test for that is better than a fixed timeout?		
	}
	
	private void resizeTheActivity() {
    	// Update layout to set the size we have decided to use instead of FILL_PARENT
        View container = findViewById(R.id.language_menu_container);
        AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)container.getLayoutParams();
        lp.width  = mPageWidth;
        lp.height = mPageHeight;
        lp.x = mPageLeft;
        lp.y = mPageTop;
        container.setLayoutParams(lp);
	}
	
	private void resizeTheActivityContents() {
		Context	context = getApplicationContext();

        View vVignette = findViewById(R.id.language_menu_vignette);
        AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams)vVignette.getLayoutParams();
        lp.x = getScaledXValue(lp.x);
        lp.y = getScaledYValue(lp.y);
        lp.width  = getScaledXValue(lp.width);
        lp.height = getScaledYValue(lp.height);
        vVignette.setLayoutParams(lp);
        
        TextView tvTitle     = (TextView)findViewById(R.id.language_menu_title);
        ViewGroup.LayoutParams lpTitle = tvTitle.getLayoutParams();
        lpTitle.height = getScaledYValue(lpTitle.height);
        tvTitle.setLayoutParams(lpTitle);
        tvTitle.setTextSize(getScaledTextSize(tvTitle.getTextSize()));
        
        // TODO: How can we override the listview item height
        // and maybe also the separator thickness
        
	}
	
	private void showTheActivityContents() {
		// Now that it is resized - show it
		// TODO: Can we fade it in?
		ViewGroup container = (ViewGroup) findViewById(R.id.language_menu_container);
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

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		String languageCode = (String)(mList.get(position).get("code"));

		trackScreenVisit(languageCode);

		if("en_classic".equalsIgnoreCase(languageCode))
		{
			doCmd_launchEveryStudent();
		}
		else
		{
			doCmd_launchInteractive(languageCode);
		}
	}

	private void doCmd_launchEveryStudent()
	{
		if (mPackageName.equalsIgnoreCase("everystudent"))
		{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("Pack", "everystudent_en_classic");

			Intent intent = new Intent(this, EveryStudent.class);
			startActivity(intent);
		}
	}

	private void doCmd_launchInteractive(String languageCode) {
		// launch SnuffyActivity which will use fixed (not downloaded) assets

		Intent intent = new Intent(this,SnuffyActivity.class);
    	intent.putExtra("PackageName" , mPackageName);
    	intent.putExtra("LanguageCode", languageCode);
    	// Also pass in the screen dimensions that we have determined
       	intent.putExtra("PageLeft"  , mPageLeft);
    	intent.putExtra("PageTop"   , mPageTop);
    	intent.putExtra("PageWidth" , mPageWidth);
    	intent.putExtra("PageHeight", mPageHeight);		
    	startActivity(intent);			
	}

	private class LanguageListAdapter extends SimpleAdapter {

		private int[] 	mColors; 
		private int		mListItemHeight;
		private float	mTextItemHeight;
		
		public LanguageListAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to, 
				int[] colors, 
				int   listItemHeight,
				float textItemHeight) {
			super(context, data, resource, from, to);
			mColors 		= colors;
			mListItemHeight = listItemHeight;
			mTextItemHeight = textItemHeight;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			int colorPos = position % mColors.length;
			view.setBackgroundResource(mColors[colorPos]);
			if (mListItemHeight > 0) {
				// we want to override list item height with a computed value
				ViewGroup.LayoutParams lp = view.getLayoutParams();
				lp.height = mListItemHeight;
				view.setLayoutParams(lp);
				
				// Also the text height
				TextView tv = (TextView)view.findViewById(R.id.list1Text);
				tv.setTextSize(mTextItemHeight);
			}
			return view;
		}		
		
	}

	private Tracker getGoogleAnalyticsTracker()
	{
		return ((SnuffyApplication)getApplication()).getTracker();
	}

	private void trackScreenVisit(String languageCode)
	{
		Tracker tracker = getGoogleAnalyticsTracker();
		tracker.setScreenName(mPackageName);
		tracker.send(new HitBuilders.AppViewBuilder()
				.setCustomDimension(1, mPackageName)
				.setCustomDimension(2, languageCode)
				.setCustomDimension(3,languageCode + "-" + mPackageName + "-" + "-1") //package-language-version_number
				.build());
	}
}
