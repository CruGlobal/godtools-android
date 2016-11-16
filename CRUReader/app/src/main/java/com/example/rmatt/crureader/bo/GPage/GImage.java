package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/31/2016.
 */

@Root(name="image")
public class GImage extends GBaseImageAttributes {


        private static final String TAG = "GImage";

        @Text(required = false)
        public String content;


        @Override
        public TextView render(ViewGroup viewGroup) {
                TextView v = new TextView(viewGroup.getContext());
                v.setText("GImage");
                Log.i(TAG, "render in GImage");
                return v;
        }
}
