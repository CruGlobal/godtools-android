package org.keynote.godtools.android.newnew.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by rmatt on 3/1/2017.
 */

public class BaseFragmentActivity extends AppCompatActivity {

    protected boolean isConfigChange;



    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        isConfigChange = true;
    }

    @Override
    public boolean isChangingConfigurations()
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
            return super.isChangingConfigurations();
        else
            return isConfigChange;
    }

}
