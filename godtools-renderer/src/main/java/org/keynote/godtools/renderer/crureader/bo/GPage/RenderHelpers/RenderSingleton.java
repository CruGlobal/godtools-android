package org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;

import java.util.Hashtable;

/**
 * Created by rmatt on 11/16/2016.
 */

public class RenderSingleton {

    public static final boolean IS_DEBUG_BUILD = true;
    public static final float KNOWN_HEIGHT = 700.0F;

    private static RenderSingleton renderSingleton;
    public SparseArray<GCoordinator> gPanelHashMap = new SparseArray<GCoordinator>();
    private Hashtable<String, Long> methodTraceMilliSecondsKeyMap = new Hashtable<String, Long>();
    /*
    Current View pager location.
     */
    public int curPosition;
    /*
    screenScalar
     */
    public float screenScalar = -1.0F;
    /*
    <Position in View Pager, background color>
     */
    public SparseArray<String> globalColors = new SparseArray<String>();
    /*
    Commonly called dimensions are hashmaped.
     */
    int screenWidth;
    int screenHeight;
    float screenDensity;


    private Context context;

    /*
    Currently rendered GDocument
     */
    private GDocument GDocument;

    private RenderSingleton(Context context) {
        this.context = context;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.density;
    }

    public static RenderSingleton getInstance() {
        return renderSingleton;
    }

    public static RenderSingleton init(Context context) {
        if (renderSingleton == null) {
            renderSingleton = new RenderSingleton(context);
        }
        return renderSingleton;
    }

    public void addGlobalColor(int position, String color) {
        globalColors.put(position, color);
    }

    public int getPositionGlobalColorAsInt(int position) {
        return Color.parseColor(globalColors.get(position, RenderConstants.DEFAULT_BACKGROUND_COLOR));
    }

    public String getPositionGlobalColorAsString(int position) {
        return globalColors.get(position);
    }

    public float getScreenHeightForNonRotationDesign() {
        if (screenScalar < 0) {
            final float screenHeightInDp = screenHeight / screenDensity;
            screenScalar = (screenHeightInDp / KNOWN_HEIGHT);
        }
        return screenScalar;
    }

    public Hashtable<String, Long> getMethodTraceMilliSecondsKeyMap() {
        return methodTraceMilliSecondsKeyMap;
    }

    public Context getContext() {
        return context;
    }

    public GDocument getGDocument() {
        return this.GDocument;
    }

    public void setGDocument(GDocument GDocument) {
        this.GDocument = GDocument;
    }
}
