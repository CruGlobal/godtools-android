package com.example.rmatt.crureader.bo.GPage;

import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/19/2016.
 */
@Root(name="buttontext")
public class GButtonText extends GBaseTextAttributes
{

    public static final String TAG = "GText";
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues();
        TextView tv = super.render(viewGroup, position);
        tv.setId(R.id.button_tv);
        return tv;
    }

    private void setDefaultValues() {
        if (size == 0 || align == "") {
            size = RenderConstants.DEFAULT_BUTTON_TEXT_SIZE;
        }

        if(align == null || align == "")
        {
            textalign = RenderConstants.DEFAULT_BUTTON_TEXT_ALIGN;
        }

        if(color == null || color == "")
        {
            color = RenderConstants.DEFAULT_TEXT_COLOR;
        }
    }

}