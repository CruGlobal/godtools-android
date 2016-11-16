package com.example.rmatt.crureader.bo.GPage;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public abstract class GBaseAttributes extends Gtapi implements IRender {

    @Attribute(required = false)
    public int x;

    @Attribute(required = false)
    public int y;

    @Attribute(required = false)
    public int w;

    @Attribute(required = false)
    public int h;

    @Attribute(required = false)
    public int xoffset;

    @Attribute(required = false)
    public int yoffset;

    @Attribute(required = false)
    public int size;


}
