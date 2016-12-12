package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GCoordinator;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 */
@Root(name = "followup-modal")
public class GFollowupModal extends GCoordinator {

    private static final String TAG = "GFollowupModal";
    @Attribute(name = "followup-id")
    public int followUpID;

    @Attribute
    public String listeners;

    @Element(name = "fallback")
    public GFallback fallback;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        return fallback.render(inflater, viewGroup, position);
    }
}
