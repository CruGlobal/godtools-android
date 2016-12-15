package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.percent.PercentRelativeLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/18/2016.
 */

@Root(name = "title")
public class GTitle extends GCoordinator {


    public static final String TAG = "GTitle";
    @Element(required = false)
    public GBaseTextAttributes heading;
    @Element(required = false)
    public GBaseTextAttributes subheading;
    @Attribute(required = false)
    public HeadingMode mode;
    @Element(required = false)
    public GBaseTextAttributes number;
    @Element(required = false, name = "peekpanel")
    public GBaseTextAttributes peekPanel;

    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        if (mode == null) mode = HeadingMode.none;

        View tempRoot = null;
        switch (mode) {
            case peek:
                tempRoot = inflater.inflate(R.layout.g_header_peak, viewGroup);

                peekPanel.updateBaseAttributes(tempRoot.findViewById(R.id.g_header_peak_peak_textview));
                break;
            case straight:
                tempRoot = inflater.inflate(R.layout.g_header_straight, viewGroup);
                break;
            case clear:
                tempRoot = inflater.inflate(R.layout.g_header_clear, viewGroup);
                break;
            case plain:
                tempRoot = inflater.inflate(R.layout.g_header_plain, viewGroup);
                break;
            default:
                tempRoot = inflater.inflate(R.layout.g_header_default, viewGroup);
                if(number != null) {
                    number.defaultColor(position);
                    number.updateBaseAttributes((AutoScaleTextView) tempRoot.findViewById(R.id.g_header_default_number_textview));
                }
                //setUpNumberTextView((AutoScaleTextView) tempRoot.findViewById(R.id.g_header_default_number_textview), position);
                break;

        }

        updateStandardRoots(tempRoot, position);

        return tempRoot.getId();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateStandardRoots(View tempRoot, int position) {


        if (tempRoot != null) {

            TextView headerTextView = (TextView) tempRoot.findViewById(R.id.g_header_header_textview);
            TextView subHeaderTextView = (TextView)tempRoot.findViewById(R.id.g_header_subheader_textview);

            boolean removeLayoutBelow = false;
            if (heading != null) {
                heading.defaultColor(position);
                heading.updateBaseAttributes(headerTextView);
            }
            if (subheading != null) {
                subheading.defaultColor(position);


                if (subheading.y != null || heading == null)     ((PercentRelativeLayout.LayoutParams)subHeaderTextView.getLayoutParams()).addRule(RelativeLayout.BELOW, -1);




                subheading.updateBaseAttributes(subHeaderTextView);
            }

        }

    }


    public enum HeadingMode {
        peek, straight, clear, plain, none
    }




}
