package com.example.rmatt.crureader.bo.GDocument;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 *     <page filename="3a0fe8aa-86bf-4ab5-82b3-4861ae71794b.xml"
 gtapi-trx-id="33a0a915-a7ab-449f-aef4-f865e1298acb" thumb="PageThumb_01.png"
 translate="true">Home
 </page>
 *
 */

@Root(name="page")
public class GDocumentPage extends Gtapi {

    @Text
    public String content;

    @Attribute
    public String filename;

    @Attribute
    public Boolean translate;

    @Attribute
    public String thumb;

    @Attribute(required = false)
    public String listeners;

    @Override
    public View render(ViewGroup viewGroup) {
        return null;
    }
}
