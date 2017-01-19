package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name = "packagename")
public class GPackageName extends GCoordinator {

    @Text
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        return 0;
    }
}
