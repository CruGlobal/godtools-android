package org.keynote.godtools.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.crittercism.app.Crittercism;

public class Splash extends Activity {
    protected boolean _active = true;
    protected int _splashTime = 2000;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Enable crash reporting
        Crittercism.initialize(getApplicationContext(), getString(R.string.key_crittercism));

        //The following was tried to help in 3.0 but didnt do the trick:
        //LinearLayout layout = (LinearLayout)findViewById(R.id.splashscreen_layout);        
        //LayoutParams lp = layout.getLayoutParams();
		//Rect r = new Rect();
	    //Window w = getWindow();
	    //w.getDecorView().getWindowVisibleDisplayFrame(r);
        //lp.height = r.height();
        //layout.setLayoutParams(lp);
        
        final Intent intent = new Intent(this,Main.class);
        
        // thread for displaying the SplashScreen
        Thread splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while(_active && (waited < _splashTime)) {
                        sleep(100);
                        if(_active) {
                            waited += 100;
                        }
                    }
                } catch(InterruptedException e) {
                    // do nothing
                } finally {
                    finish();
                    startActivity(intent);
                }
            }
        };
        splashThread.start();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _active = false;
        return true;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg)
    {
    	_active = false;
    	return true;
    }
}