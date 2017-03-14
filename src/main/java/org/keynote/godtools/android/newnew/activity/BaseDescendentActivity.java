package org.keynote.godtools.android.newnew.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.keynote.godtools.android.R;

/**
 * Created by rmatt on 3/13/2017.
 */

public abstract class BaseDescendentActivity extends AppCompatActivity {


    protected Toolbar mToolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState == null) {
            setIntentData();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, getFragment()).commit();
        }
    }

    protected abstract Fragment getFragment();
    protected abstract void setIntentData();

}
