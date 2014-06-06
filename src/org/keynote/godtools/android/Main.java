package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.HttpTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class Main extends Activity implements HttpTask.HttpTaskHandler {
	private static final String TAG = "Main";

	private static final String PREFS_NAME = "MAIN";
	//public static final int		REFERENCE_DEVICE_HEIGHT = 920;	// pixels on iPhone w/retina - not including title bar
	public static final int		REFERENCE_DEVICE_HEIGHT = 960;	// pixels on iPhone w/retina - including title bar
	public static final int		REFERENCE_DEVICE_WIDTH  = 640;	// pixels on iPhone w/retina - full width

	private int					mPageLeft;
	private int					mPageTop;
	private int					mPageWidth;
	private int					mPageHeight;
	private boolean				mSetupNeeded;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

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

		// restore state from prefs
		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		//E.g.:
		//mPagerCurrentItem = settings.getInt   ("currPage", 0);
		//setLanguage        (settings.getString("currLanguageCode", getLanguageDefault()));
		mSetupNeeded = true;
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
		// TODO: Ought this to be replaced by some sort of app-wide storage
       	intent.putExtra("PageLeft"  , mPageLeft);
    	intent.putExtra("PageTop"   , mPageTop);
    	intent.putExtra("PageWidth" , mPageWidth);
    	intent.putExtra("PageHeight", mPageHeight);
	}

	public void onCmd_kgp(View view)
	{
    	Intent intent = new Intent(this,LanguagesActivity.class);
    	intent.putExtra("PackageName", "kgp");
    	addPageFrameToIntent(intent);
     	startActivity(intent);
	}

	public void onCmd_satisfied(View view)
	{
		Intent intent = new Intent(this,LanguagesActivity.class);
    	intent.putExtra("PackageName", "satisfied");
    	addPageFrameToIntent(intent);
    	startActivity(intent);
	}

	public void onCmd_fourlaws(View view)
	{
		Intent intent = new Intent(this,LanguagesActivity.class);
    	intent.putExtra("PackageName", "fourlaws");
    	addPageFrameToIntent(intent);
    	startActivity(intent);
	}

	public void onCmd_everystudent(View view)
	{
    	Intent intent = new Intent(this,LanguagesActivity.class);
    	intent.putExtra("PackageName", "everystudent");
    	addPageFrameToIntent(intent);
    	startActivity(intent);
	}

	public void onCmd_settings(View view) {
	   	Intent intent = new Intent(this,About.class);
    	startActivity(intent);
	}

    private void quit()
    {
       super.onDestroy();
       this.finish();
    }

    private List<GTPackage> getPackages(String langCode){
        return GTPackage.getPackageByLanguage(getApplicationContext(), langCode);
    }

    private void getPackagesFromServer(String langCode){
        GodToolsApiClient.getListOfPackages(langCode, "tag", this);
    }

    @Override
    public void httpTaskComplete(String url, InputStream is, int statusCode, String tag) {
        List<GTPackage> packageList = processResponse(is);
    }

    @Override
    public void httpTaskFailure(String url, InputStream is, int statusCode, String tag) {

    }

    private List<GTPackage> processResponse(InputStream is){

        List<GTPackage> packageList = new ArrayList<GTPackage>();

        Document xmlDoc;
        try {
            xmlDoc = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(is);
            xmlDoc.normalize();

            NodeList nlLanguage = xmlDoc.getElementsByTagName("language");
            String language = ((Element)nlLanguage.item(0)).getAttribute("code");

            NodeList nlPackages = xmlDoc.getElementsByTagName("package");
            int numPackages = nlPackages.getLength();

            for (int i=0; i<numPackages; i++) {
                Element element = (Element)nlPackages.item(i);

                String name = element.getAttribute("name");
                String code = element.getAttribute("code");
                int version = Integer.valueOf(element.getAttribute("version"));

                GTPackage gtp = new GTPackage();
                gtp.setCode(code);
                gtp.setName(name);
                gtp.setVersion(version);
                gtp.setLanguage(language);

                packageList.add(gtp);
            }

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }


        return packageList;
    }
}