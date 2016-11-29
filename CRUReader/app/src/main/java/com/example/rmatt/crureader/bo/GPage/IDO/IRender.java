package com.example.rmatt.crureader.bo.GPage.IDO;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by rmatt on 11/2/2016.
 */

public interface IRender<T> {

    T render(ViewGroup viewGroup, int position);

}
