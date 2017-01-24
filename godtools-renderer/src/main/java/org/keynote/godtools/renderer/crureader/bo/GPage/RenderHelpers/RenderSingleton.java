package org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import org.keynote.godtools.renderer.crureader.AlertDialogActivity;
import org.keynote.godtools.renderer.crureader.BaseAppConfig;
import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;

import java.util.ArrayList;
import java.util.Hashtable;

import static org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes.ButtonMode.phone;


public class RenderSingleton {

    public static final boolean IS_DEBUG_BUILD = false;
    public static final float KNOWN_HEIGHT = 700.0F;

    private static RenderSingleton renderSingleton;


    /*
    This holds page local events
     */
    public final SparseArray<GCoordinator> gPanelHashMap = new SparseArray<>();


    /*
    This holds urls on page
     */
    public final ArrayList<String> urls = new ArrayList<>();

    /*
    screenScalar
     */
    public float screenScalar = -1.0F;
    /*
    <Position in View Pager, background color>
     */
    public final SparseArray<String> globalColors = new SparseArray<String>();
    /*
    Commonly called dimensions are hashmaped.
     */
    final int screenWidth;
    final int screenHeight;
    final float screenDensity;
    private final Hashtable<String, Long> methodTraceMilliSecondsKeyMap = new Hashtable<String, Long>();
    private final Context context;
    /*
    Currently rendered GDocument
     */
    private GDocument GDocument;

    private BaseAppConfig baseAppConfig;
    final View.OnClickListener mLinksOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            final GBaseButtonAttributes.ButtonMode mode = GBaseButtonAttributes.ButtonMode.valueOf((String) v.getTag(R.id.button_mode));
            final String content = (String) v.getTag(R.id.button_content);
            String urlScheme;
            Intent intent;
            switch (mode) {

                case email:

                case phone:

                    urlScheme = mode == phone ? "tel:" : "mailto:";
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme + content));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    RenderSingleton.getInstance().getContext().startActivity(intent);
                    break;
                case allurl:
                    intent = new Intent(RenderSingleton.getInstance().getContext(), AlertDialogActivity.class);

                    intent.putExtra(AlertDialogActivity.CONSTANTS_ALERT_DIALOG_CONTENT_STRING_EXTRA, RenderSingleton.getInstance().getUrlsFormatted());
                    intent.putExtra(AlertDialogActivity.CONSTANTS_ALERT_DIALOG_MODE_STRING_EXTRA, mode.toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    RenderSingleton.getInstance().getContext().startActivity(intent);
                    Log.i("allurl", "allurl: " + RenderSingleton.getInstance().getUrlsFormatted());


                    break;

                default:
                    intent = new Intent(RenderSingleton.getInstance().getContext(), AlertDialogActivity.class);
                    intent.putExtra(AlertDialogActivity.CONSTANTS_ALERT_DIALOG_CONTENT_STRING_EXTRA, content);
                    intent.putExtra(AlertDialogActivity.CONSTANTS_ALERT_DIALOG_MODE_STRING_EXTRA, mode.toString());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    RenderSingleton.getInstance().getContext().startActivity(intent);
                    break;
            }

        }
    };

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

    public GPage getPages(int mPosition) {
        return getGDocument().getPages().get(mPosition);
    }



    public void setBaseAppConfig(BaseAppConfig baseAppConfig) {
        this.baseAppConfig = baseAppConfig;
    }

    public BaseAppConfig getAppConfig() {
        return baseAppConfig;
    }

    public String getUrlsFormatted() {
        String urlContent = "";
        Log.i("allurl", "urls size " + urls.size());
        for(String url : urls)
        {
            Log.i("allurl", "url" + urls.size());
            urlContent += url + "\n";
        }
        return urlContent;
    }
}
