package org.keynote.godtools.android.utils;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;

import org.keynote.godtools.android.Gallery;


/**
 * Listener for controlling zoom state through touch events
 */
public class KeyListener implements View.OnKeyListener
{

    Gallery gallery;

    public KeyListener(Context context, Gallery galleryin)
    {
        gallery = galleryin;
    }

    public boolean onKey(View v, int keyCode, KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_UP)
        {
            switch (keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    gallery.prevImage();
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    gallery.nextImage();
                    return true;
            }
        }

        return false;
    }


}
