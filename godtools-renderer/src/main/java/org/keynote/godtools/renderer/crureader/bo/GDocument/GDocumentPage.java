package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 * <page filename="3a0fe8aa-86bf-4ab5-82b3-4861ae71794b.xml"
 * gtapi-trx-id="33a0a915-a7ab-449f-aef4-f865e1298acb" thumb="PageThumb_01.png"
 * translate="true">Home
 * </page>
 */

@Root(name = "page")
public class GDocumentPage extends GCoordinator {

    @Text
    public String content;

    @Attribute
    public String filename;

    @Attribute
    public String thumb;

    @Attribute(required = false)
    public String listeners;


    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        return 0;
    }
}
