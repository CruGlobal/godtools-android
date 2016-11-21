package com.example.rmatt.crureader.bo;

import android.view.View;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/24/2016.
 */

public abstract class Gtapi implements IRender {

    @Attribute(name = "gtapi-trx-id", required = false)
    public String gtapiTrxId;

    @Attribute(name = "tnt-trx-ref-value", required = false)
    public String tntTrxRefValue;

    @Attribute(name = "tnt-trx-translated", required = false)
    public String tntTrxTranslated;


    @Attribute(required = false)
    public String translate;

    private void setDefaultPadding(View view) {

        view.setPadding(20, 20, 20, 20);
    }

}
