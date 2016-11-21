package com.example.rmatt.crureader.bo.GPage;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public abstract class GBaseImageAttributes extends GBaseAttributes {

    @Attribute(required = false)
    public String align;

}
