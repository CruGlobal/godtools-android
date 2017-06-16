package org.cru.godtools.base.ui.activity;

import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    /* BEGIN lifecycle */

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
    }

    /* END lifecycle */
}
