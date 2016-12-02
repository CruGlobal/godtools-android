package com.example.rmatt.crureader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.LinearLayout;

/**
 * Created by rmatt on 11/23/2016.
 */

public class RenderSingleTestActivity extends FragmentActivity {


    private static String singleXMLTestID = "c7b98fdf-ff4f-41b6-bd37-fead336b0a23.xml";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_singlerender);
        LinearLayout ll = (LinearLayout) findViewById(R.id.singlerendertest);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.replace(R.id.singlerendertest, SlidePageFragment.create(0, singleXMLTestID));

        fragmentTransaction.commit();

    }
}
