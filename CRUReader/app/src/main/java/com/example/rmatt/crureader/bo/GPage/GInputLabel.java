package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 * <input-label modifier="bold" size="80">Email</input-label>
 */
@Root(name = "input-label")
public class GInputLabel extends GBaseTextAttributes {

    private static final String TAG = "GInputLabel";

    @Override
    public TextView render(ViewGroup viewGroup, int position) {

        super.render(viewGroup, position);
        Context context = viewGroup.getContext();
        TextView v = new TextView(context);
        v.setText("GInputLabel");
        Log.i(TAG, "render in GInputLabel");
        return v;
    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }
}
