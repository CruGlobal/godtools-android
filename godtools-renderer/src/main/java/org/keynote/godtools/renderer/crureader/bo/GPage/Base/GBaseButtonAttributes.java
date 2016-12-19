package org.keynote.godtools.renderer.crureader.bo.GPage.Base;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/25/2016.
 */

public abstract class GBaseButtonAttributes extends GCoordinator {

    private static final String TAG = "GBaseButtonAttributes";

    @Attribute(required = false, name = "size")
    public Integer textSize;

    @Attribute(required = false)
    public String validation;

    @Attribute(required = false)
    public ButtonMode mode;

    @Attribute(name = "tap-events", required = false)
    public String tapEvents;

    public enum ButtonMode {
        big, url, allurl, email, link
    }

}
