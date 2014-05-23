package org.keynote.godtools.android;

import java.util.HashMap;

import org.keynote.godtools.android.utils.BasicZoomControl;
//import org.keynote.godtools.android.utils.FlurryAPI;
import org.keynote.godtools.android.utils.ImageZoomView;
import org.keynote.godtools.android.utils.KeyListener;
import org.keynote.godtools.android.utils.LongPressZoomListener;

//import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.PowerManager;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class Gallery extends Activity {

	//private static final String TAG = "GALLERY";
	
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
    private Animation slideRightOut;
    private ViewFlipper viewFlipper;
    
    private Bitmap mBitmap0;
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;
    
    private ImageZoomView mZoomView0;
    private ImageZoomView mZoomView1;
    private ImageZoomView mZoomView2;
    
    private BasicZoomControl mZoomControl0;
    private BasicZoomControl mZoomControl1;
    private BasicZoomControl mZoomControl2;
    
    private LongPressZoomListener mZoomListener0;
    private LongPressZoomListener mZoomListener1;
    private LongPressZoomListener mZoomListener2;
    
    private KeyListener mKeyListener;
    
    protected int[] images = {};
    
    private int curImage = 0;
    private int numViews = 0;
    
    private PowerManager.WakeLock wl = null;
    
    private BitmapFactory.Options opt = new BitmapFactory.Options();
    
    protected String curLogTag = "Gallery";
    
    private long lastTime = System.currentTimeMillis();
    private int  lastPage = 1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences settings = getSharedPreferences(Settings.PREFNAME, 0);
        if (settings.getBoolean("wakelock", true)) {
        	PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
            wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");  
        }
        
        setScreen();
        
        boolean nextImage = false;
        boolean lastImage = false;
        
        setContentView(R.layout.gallery);
        viewFlipper = (ViewFlipper)findViewById(R.id.gallery_flipper);
        slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
        
        if (images.length > 0) {
	    	byte[] byteArrayForBitmap = new byte[17*1024];
	    	opt.inTempStorage =  byteArrayForBitmap;
	    	
	    	int num = 0;
	    	if (images.length > 2) {
	    		num = 2;
	    		numViews = 3;
	    	} if (images.length == 2) {
	    		num = 1;
	    		numViews = 2;
	    		viewFlipper.removeViewAt(2);
	    	} if (images.length == 1) {
	    		num = 0;
	    		numViews = 1;
	    		viewFlipper.removeViewAt(2);
	    		viewFlipper.removeViewAt(1);
	    	}
	    	
	    	mKeyListener = new KeyListener (getApplicationContext(), this);
	    	
	    	if (numViews > 2) {
	    		if (savedInstanceState != null) {
			        if (savedInstanceState.containsKey("curImage"))
			        	curImage = savedInstanceState.getInt("curImage");
			        
			        if (numViews > 2 ) {
				        if (curImage > 0 && curImage != images.length - 1) {
				        	curImage = curImage-1;
				        	nextImage = true;
				        } else if (curImage == images.length - 1) {
				        	curImage = curImage - 2;
				        	lastImage = true;
				        }
			        } else {
			        	if (curImage > 0) {
				        	curImage = curImage-1;
				        	nextImage = true;
				        }
			        }
	    		}
	    	}

	    	
	    	switch (num) {
		    	case 2:
		    		//Log.i(TAG, "CREATING VIEW 2");
		            mZoomControl2 = new BasicZoomControl();
		            mBitmap2 = BitmapFactory.decodeResource(getResources(), images[curImage+2], opt);
		            mZoomListener2 = new LongPressZoomListener(getApplicationContext(), this);
		            mZoomListener2.setZoomControl(mZoomControl2);
		            mZoomView2 = (ImageZoomView)findViewById(R.id.gallery_image2);
		            mZoomView2.setZoomState(mZoomControl2.getZoomState());
		            mZoomView2.setImage(mBitmap2);
		            mZoomView2.setOnTouchListener(mZoomListener2);
		            mZoomView2.setOnKeyListener(mKeyListener);
		            mZoomControl2.setAspectQuotient(mZoomView2.getAspectQuotient());
		            resetZoomState(2);
		    	case 1:
		    		//Log.i(TAG, "CREATING VIEW 1");
		    		mZoomControl1 = new BasicZoomControl();
		    		mBitmap1 = BitmapFactory.decodeResource(getResources(), images[curImage+1], opt);
		            mZoomListener1 = new LongPressZoomListener(getApplicationContext(), this);
		            mZoomListener1.setZoomControl(mZoomControl1);
		            mZoomView1 = (ImageZoomView)findViewById(R.id.gallery_image1);
		            mZoomView1.setZoomState(mZoomControl1.getZoomState());
		            mZoomView1.setImage(mBitmap1);
		            mZoomView1.setOnTouchListener(mZoomListener1);
		            mZoomView1.setOnKeyListener(mKeyListener);
		            mZoomControl1.setAspectQuotient(mZoomView1.getAspectQuotient());
		            resetZoomState(1);
		    	case 0:
		    		//Log.i(TAG, "CREATING VIEW 0");
		    		mZoomControl0 = new BasicZoomControl();
		    		mBitmap0 = BitmapFactory.decodeResource(getResources(), images[curImage], opt);
		            mZoomListener0 = new LongPressZoomListener(getApplicationContext(), this);
		            mZoomListener0.setZoomControl(mZoomControl0);
		            mZoomView0 = (ImageZoomView)findViewById(R.id.gallery_image0);
		            mZoomView0.setZoomState(mZoomControl0.getZoomState());
		            mZoomView0.setImage(mBitmap0);
		            mZoomView0.setOnTouchListener(mZoomListener0);
		            mZoomView0.setOnKeyListener(mKeyListener);
		            mZoomControl0.setAspectQuotient(mZoomView0.getAspectQuotient());
		            resetZoomState(0);
		    }
    	} else {
    		//Log.i(TAG, "REMOVING ALL VIEWS");
    		viewFlipper.removeAllViews();
    		Toast.makeText(this, R.string.gallery_no_images , Toast.LENGTH_LONG).show();
    	}
        
        if (nextImage) {
        	nextImage();
        }
        
        if (lastImage) {
        	nextImage();
        	nextImage();
        }
        
        logPageView();
    }
    
    protected void logTime() {
    	long viewtime = (System.currentTimeMillis() - lastTime) / 1000;
    	lastTime = System.currentTimeMillis();
    	if (viewtime > 0) {
	    	HashMap<String,String> params = new HashMap<String,String>();
	    	params.put("Time", String.valueOf(lastPage) + ":" + String.valueOf(viewtime));
	    	lastPage = curImage+1;
	    	//FlurryAgent.onEvent(FlurryAPI.FlurryPrefix + curLogTag+"Time", params);
    	}
    }
    
    protected void logPageView() {
    	HashMap<String,String> params = new HashMap<String,String>();
    	params.put("Page", String.valueOf(curImage+1));
    	//FlurryAgent.onEvent(FlurryAPI.FlurryPrefix + curLogTag, params);
    	//FlurryAgent.onPageView();
    	logTime();
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		logTime();
		if (wl != null)
			wl.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		lastTime = System.currentTimeMillis();
		if (wl != null)
			wl.acquire();
	}
    
    public void setScreen() {
    	getResources().getConfiguration();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	}
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt("curImage", curImage);
      super.onSaveInstanceState(savedInstanceState);
    }
    
    private synchronized int getNextFlipId(int curId) {
    	switch (curId) {
    		case R.id.gallery_image0: return R.id.gallery_image1;
    		case R.id.gallery_image1: return R.id.gallery_image2;
    		case R.id.gallery_image2: return R.id.gallery_image0;
    	}
    	return R.id.gallery_image0;
    }
    
    private synchronized int getPrevFlipId(int curId) {
    	switch (curId) {
    		case R.id.gallery_image0: return R.id.gallery_image2;
    		case R.id.gallery_image1: return R.id.gallery_image0;
    		case R.id.gallery_image2: return R.id.gallery_image1;
    	}
    	return R.id.gallery_image0;
    }
    
    public synchronized void nextImage() {
    	if (numViews == 2) {
            resetZoomState(0);
            resetZoomState(1);
		} else if (numViews == 3) {
			resetZoomState(0);
            resetZoomState(1);
            resetZoomState(2);
		}
    	
		if (numViews > 1 && curImage+1 < images.length) {
			curImage++;
	    	viewFlipper.setInAnimation(slideLeftIn);
	        viewFlipper.setOutAnimation(slideLeftOut);
	    	viewFlipper.showNext();
		}
		
    	if (numViews == 3 && curImage < images.length) {
    		if (numViews == 3 && curImage > 0) {
    			
    			//Log.i(TAG, "Moving VIEW1 to VIEW0");
    			mZoomControl0 = mZoomControl1;
    			mZoomListener0 = mZoomListener1;
    			mZoomListener0.setZoomControl(mZoomControl0);
    			mBitmap0 = mBitmap1;
    			mZoomView0 = mZoomView1;
	            mZoomView0.setZoomState(mZoomControl0.getZoomState());
	            mZoomView0.setImage(mBitmap0);
	            mZoomView0.setOnTouchListener(mZoomListener0);
	            mZoomControl0.setAspectQuotient(mZoomView0.getAspectQuotient());
	            resetZoomState(0);
    			
    			//Log.i(TAG, "Moving VIEW2 to VIEW1");
    			mZoomControl1 = mZoomControl2;
    			mZoomListener1 = mZoomListener2;
    			mZoomListener1.setZoomControl(mZoomControl1);
    			mBitmap1 = mBitmap2;
    			mZoomView1 = mZoomView2;
	            mZoomView1.setZoomState(mZoomControl1.getZoomState());
	            mZoomView1.setImage(mBitmap1);
	            mZoomView1.setOnTouchListener(mZoomListener1);
	            mZoomControl1.setAspectQuotient(mZoomView1.getAspectQuotient());
	            resetZoomState(1);
    			
	            if (curImage + 1 != images.length) {
		    		//Log.i(TAG, "CREATING VIEW 2");
		    		mZoomControl2 = new BasicZoomControl();
		            mZoomListener2 = new LongPressZoomListener(getApplicationContext(), this);
		            mZoomListener2.setZoomControl(mZoomControl2);
		            mBitmap2 = BitmapFactory.decodeResource(getResources(), images[curImage+1], opt);
		            mZoomView2 = (ImageZoomView)findViewById(getNextFlipId(viewFlipper.getCurrentView().getId()));
		            mZoomView2.setZoomState(mZoomControl2.getZoomState());
		            mZoomView2.setImage(mBitmap2);
		            mZoomView2.setOnTouchListener(mZoomListener2);
		            mZoomView2.setOnKeyListener(mKeyListener);
		            mZoomControl2.setAspectQuotient(mZoomView2.getAspectQuotient());
		            resetZoomState(2);
	            }
    		}
    	}
    	logPageView();
    }
    
    public synchronized void prevImage() {
    	if (numViews == 2) {
            resetZoomState(0);
            resetZoomState(1);
		} else if (numViews == 3) {
			resetZoomState(0);
            resetZoomState(1);
            resetZoomState(2);
		}
    	
		if (numViews > 1 && curImage > 0) {
			curImage--;
	    	viewFlipper.setInAnimation(slideRightIn);
	        viewFlipper.setOutAnimation(slideRightOut);
	    	viewFlipper.showPrevious();
		}
		
    	if (curImage < images.length-1 && curImage != 0) {
    		if (numViews == 3 && curImage < images.length-1) {
    			
    			mZoomControl1 = mZoomControl0;
    			mZoomListener1 = mZoomListener0;
    			mZoomListener1.setZoomControl(mZoomControl1);
    			mBitmap1 = mBitmap0;
    			mZoomView1 = mZoomView0;
	            mZoomView1.setZoomState(mZoomControl1.getZoomState());
	            mZoomView1.setImage(mBitmap1);
	            mZoomView1.setOnTouchListener(mZoomListener1);
	            mZoomControl1.setAspectQuotient(mZoomView1.getAspectQuotient());
	            resetZoomState(1);
	            
    			mZoomControl2 = mZoomControl1;
    			mZoomListener2 = mZoomListener1;
    			mZoomListener2.setZoomControl(mZoomControl2);
    			mBitmap2 = mBitmap1;
    			mZoomView2 = mZoomView1;
	            mZoomView2.setZoomState(mZoomControl2.getZoomState());
	            mZoomView2.setImage(mBitmap2);
	            mZoomView2.setOnTouchListener(mZoomListener2);
	            mZoomControl2.setAspectQuotient(mZoomView2.getAspectQuotient());
	            resetZoomState(2);
	            
	    		mZoomControl0 = new BasicZoomControl();
	            mZoomListener0 = new LongPressZoomListener(getApplicationContext(), this);
	            mZoomListener0.setZoomControl(mZoomControl0);
	            mBitmap0 = BitmapFactory.decodeResource(getResources(), images[curImage-1], opt);
	            mZoomView0 = (ImageZoomView)findViewById(getPrevFlipId(viewFlipper.getCurrentView().getId()));
	            mZoomView0.setZoomState(mZoomControl0.getZoomState());
	            mZoomView0.setImage(mBitmap0);
	            mZoomView0.setOnTouchListener(mZoomListener0);
	            mZoomView0.setOnKeyListener(mKeyListener);
	            mZoomControl0.setAspectQuotient(mZoomView0.getAspectQuotient());
	            resetZoomState(0);
    		}
    	}
    	logPageView();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBitmap0 != null) {
        	mBitmap0.recycle();
        	mZoomView0.setOnTouchListener(null);
        	mZoomView0.setOnKeyListener(null);
        	mZoomControl0.getZoomState().deleteObservers();
        }
        if (mBitmap1 != null) {
        	mBitmap1.recycle();
        	mZoomView1.setOnTouchListener(null);
        	mZoomView1.setOnKeyListener(null);
        	mZoomControl1.getZoomState().deleteObservers();
        }
        if (mBitmap2 != null) {
        	mBitmap2.recycle();
        	mZoomView2.setOnTouchListener(null);
        	mZoomView2.setOnKeyListener(null);
        	mZoomControl2.getZoomState().deleteObservers();
        }
        logTime();
    }
    
    private void resetZoomState(int view) {
    	BasicZoomControl mZoomControl = null;
    	ImageZoomView mZoomView = null;
    	switch (view) {
	    	case 0: mZoomControl = mZoomControl0; mZoomView = mZoomView0; break;
	    	case 1: mZoomControl = mZoomControl1; mZoomView = mZoomView1; break;
	    	case 2: mZoomControl = mZoomControl2; mZoomView = mZoomView1; break;
    	}
    	if (mZoomControl != null) {
    		if (mZoomView != null && mZoomView.getWidth() > mZoomView.getHeight()){
    			mZoomControl.getZoomState().setZoom(1f / mZoomView.getAspectQuotient().get());
    			mZoomControl.getZoomState().setPanX(0.5f);
    	        mZoomControl.getZoomState().setPanY(.5f - mZoomControl.getMaxPanDelta(mZoomControl.getZoomState().getZoomY(mZoomView.getAspectQuotient().get())));
    		} else {
    			mZoomControl.getZoomState().setZoom(1f);
    			mZoomControl.getZoomState().setPanX(0.5f);
    	        mZoomControl.getZoomState().setPanY(0.5f);
    		}
	        mZoomControl.getZoomState().notifyObservers();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_help:
        		Intent helpIntent = new Intent(this,GalleryHelp.class);
            	startActivity(helpIntent);
                return true;
        }
        
        return false;
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