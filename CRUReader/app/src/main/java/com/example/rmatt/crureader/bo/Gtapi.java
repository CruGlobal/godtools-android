package com.example.rmatt.crureader.bo;

import android.view.View;
import android.view.ViewGroup;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/24/2016.
 */

public abstract class Gtapi<T extends View, G extends ViewGroup> {

    @Attribute(name = "gtapi-trx-id", required = false)
    public String gtapiTrxId;

    @Attribute(name = "tnt-trx-ref-value", required = false)
    public String tntTrxRefValue;

    @Attribute(name = "tnt-trx-translated", required = false)
    public String tntTrxTranslated;

    @Attribute(required = false)
    public String translate;

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

    @Attribute(required = false)
    public String align;

    @Attribute(name = "x-trailing-offset", required = false)
    public int xTrailingOffset;


    private void setDefaultPadding(View view) {

        view.setPadding(20, 20, 20, 20);
    }


    public abstract T render(ViewGroup viewGroup, int position);
    public G group(G viewGroup, int position)
    {
        return viewGroup;
    }


}
