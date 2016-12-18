package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 * <followup-body modifier="bold" x="0" x-trailing-offset="30" xoffset="30" yoffset="40">
 * Knowing someone better helps a relationship grow. Would you like to sign up for an
 * email series that can help guide you in your relationship with Jesus Christ?
 * </followup-body>
 */
@Root(name = "followup-body")
public class GFollowUpBody extends GBaseTextAttributes {

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View inflatedView = inflater.inflate(R.layout.g_followup_body, viewGroup);

        AutoScaleTextView tv = (AutoScaleTextView)inflatedView.findViewById(R.id.g_followup_body_textview);
        tv.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(tv);
        return tv.getId();
    }


}
