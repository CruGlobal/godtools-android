package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/26/2016.
 * <input-placeholder>john.doe@gmail.com</input-placeholder>
 */
@Root(name="input-placeholder")
public class GInputPlaceholder implements IRender{

    private static final String TAG = "GInputPlaceholder";
    @Text
    public String content;

    @Override
    public TextView render(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        TextView v = new TextView(context);
        v.setText("GInputPlaceholder");
        Log.i(TAG, "render in GInputPlaceholder");
        return v;
    }
}
