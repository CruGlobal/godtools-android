package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 *
 *   <question gtapi-trx-id="fae69b3b-e95b-4184-9bcd-211e35faa71c" translate="true">Why do you think most people
 don't know God personally?</question>
 */
@Root(name="question")
public class GQuestion extends GBaseTextAttributes {

    public static final String TAG = "GQuestion";

    @Attribute(required = false)
    public String mode;

    @Override
    public TextView render(ViewGroup viewGroup) {

        return super.render(viewGroup);
    }


}
