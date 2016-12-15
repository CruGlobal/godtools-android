package com.example.rmatt.crureader.bo.GPage;

import com.example.rmatt.crureader.bo.GCoordinator;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 12/15/2016.
 */

public abstract  class GModal extends GCoordinator {
    @Attribute
    public String listeners;


}
