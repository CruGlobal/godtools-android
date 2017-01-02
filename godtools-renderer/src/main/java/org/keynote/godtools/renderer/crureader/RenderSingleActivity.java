package org.keynote.godtools.renderer.crureader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by rmatt on 11/23/2016.
 */

public class RenderSingleActivity extends FragmentActivity {

    public static final String FILE_ID_STRING_EXTRA = "fileidstringextra";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_singlerender);

        String fileID = getIntent().getExtras().getString(FILE_ID_STRING_EXTRA);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, SlidePageFragment.create(0, fileID));
        fragmentTransaction.commit();

    }
}
