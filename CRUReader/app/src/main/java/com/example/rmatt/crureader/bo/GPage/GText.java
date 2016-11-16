package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import static android.content.ContentValues.TAG;

@Root(name="text")
public class GText extends GBaseTextAttributes implements IRender
{
    public static final String TAG = "GText";


    @Override
    public TextView render(ViewGroup viewGroup) {
        setDefaultValues();
        return super.render(viewGroup);
    }

    private void setDefaultValues() {
        if (size == 0) {
            size = RenderConstants.DEFAULT_TEXT_SIZE;
        }
    }
}