package org.keynote.godtools.android.snuffy;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.utils.FileUtils;
import org.keynote.godtools.renderer.crureader.SlidePageFragment;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import java.io.File;

public class SnuffyAboutActivity extends FragmentActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_snuffy);


        File fileForGDP = new File(FileUtils.getResourcesDir(SnuffyAboutActivity.this), RenderSingleton.getInstance().getGDocument().about.filename);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content_container_real, SlidePageFragment.create(0, fileForGDP.getAbsolutePath()));
        fragmentTransaction.commit();

    }

}
