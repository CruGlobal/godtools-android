package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 * <packagename gtapi-trx-id="45e65db9-957f-4ca5-b4ca-b60c4ce424c1" translate="true">Knowing God Personally</packagename>
 */

@Root(name = "packagename")
public class GPackageName extends GCoordinator {

    @Text
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        return 0;
    }
}
