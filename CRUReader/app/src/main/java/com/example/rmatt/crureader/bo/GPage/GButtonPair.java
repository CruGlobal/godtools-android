package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import static android.content.ContentValues.TAG;

/**
 * Created by rmatt on 10/25/2016.
 */
@Root(name="button-pair")
public class GButtonPair extends GBaseButtonAttributes {



    @Element(name="positive-button", required = false)
    public GBaseButtonAttributes positiveButton;

    @Element(name="negative-button", required = false)
    public GBaseButtonAttributes negativeButton;

    @Override
    public LinearLayout render(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        LinearLayout ll = new LinearLayout(context);

        TextView v = new TextView(context);
        v.setText("GBaseButtonAttributes");
        ll.addView(v, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Log.i(TAG, "render in GBaseButtonAttributes");
        return ll;
    }
}
