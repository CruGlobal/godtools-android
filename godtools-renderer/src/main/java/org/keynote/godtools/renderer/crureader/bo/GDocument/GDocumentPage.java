package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

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
