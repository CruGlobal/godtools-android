package org.keynote.godtools.renderer.crureader.bo.GPage.IDO;

import android.view.ViewGroup;

/**
 * Created by rmatt on 11/2/2016.
 */

public interface IRender<T> {

    T render(ViewGroup viewGroup, int position);

}
