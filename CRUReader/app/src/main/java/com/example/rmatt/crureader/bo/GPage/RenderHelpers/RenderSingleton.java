package com.example.rmatt.crureader.bo.GPage.RenderHelpers;

import android.content.Context;

import com.example.rmatt.crureader.RenderApp;
import com.example.rmatt.crureader.bo.GPage.GPanel;

import java.util.HashMap;

/**
 * Created by rmatt on 11/16/2016.
 */

public class RenderSingleton {


    private Context context;

    public HashMap<Integer, GPanel> gPanelHashMap = new HashMap<Integer, GPanel>();
    public String globalColor;

    private static RenderSingleton renderSingleton;

    private RenderSingleton(Context context)
    {
        this.context = context;
    }

    public static RenderSingleton getInstance()
    {
        return renderSingleton;
    }

    public static RenderSingleton init(Context context) {
        if(renderSingleton == null)
        {
            renderSingleton = new RenderSingleton(context);
        }
        return renderSingleton;
    }
}
