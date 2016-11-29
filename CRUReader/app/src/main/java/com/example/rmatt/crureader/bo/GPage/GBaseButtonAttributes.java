package com.example.rmatt.crureader.bo.GPage;

import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/25/2016.
 */

public abstract class GBaseButtonAttributes extends Gtapi<AppCompatButton, ViewGroup> {




    private static final String TAG = "GBaseButtonAttributes";
    @Attribute(required = false)
    public String validation;

    @Attribute(required = false)
    public String mode;

    @Attribute(name = "tap-events", required = false)
    public String tapEvents;





}
