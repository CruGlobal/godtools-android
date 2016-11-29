package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import static android.content.ContentValues.TAG;

/**
 * Created by rmatt on 10/26/2016.
 *    <followup-body modifier="bold" x="0" x-trailing-offset="30" xoffset="30" yoffset="40">
 Knowing someone better helps a relationship grow. Would you like to sign up for an
 email series that can help guide you in your relationship with Jesus Christ?
 </followup-body>
 */
@Root(name="followup-body")
public class GFollowUpBody extends GBaseTextAttributes {

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView tv = new TextView(viewGroup.getContext());
        Log.i(TAG, "GButtonPair render");
        tv.setText("Not implemented yet");
        return tv;
    }


}
