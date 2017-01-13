package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.app.Activity;
import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.captain_miao.optroundcardview.OptRoundCardView;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/18/2016.
 */

@Root(name = "title")
public class GTitle extends GCoordinator {

    public static final String TAG = "GTitle";
    @Element(required = false)
    public GBaseTextAttributes heading;
    @Element(required = false)
    public GBaseTextAttributes subheading;
    @Attribute(required = false)
    public HeadingMode mode;
    @Element(required = false)
    public GBaseTextAttributes number;
    @Element(required = false, name = "peekpanel")
    public GBaseTextAttributes peekPanel;
    boolean autoTextResizeHeader = false;
    private int zie = 30;

    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        final Context context = viewGroup.getContext();
        if (mode == null) mode = HeadingMode.none;

        View tempRoot = null;
        switch (mode) {
            case peek:
                tempRoot = inflater.inflate(R.layout.g_header_peak, viewGroup);
                autoTextResizeHeader = true;
                final OptRoundCardView headerTopRoundCardView = (OptRoundCardView) tempRoot.findViewById(R.id.g_header_peek_outerlayout_optroundcardview);
                // final OptRoundCardView peekPanelRoundCardView = (OptRoundCardView) tempRoot.findViewById(R.id.g_header_peek_peeklayout_optroundcardview);
                //peekPanelRoundCardView.setClipToPadding(true);
                //final AutoScaleTextView autoScaleTextView = (AutoScaleTextView) tempRoot.findViewById(R.id.g_header_peak_peak_textview);

                //autoScaleTextView.setId(RenderViewCompat.generateViewId());
                //peekPanelRoundCardView.setId(RenderViewCompat.generateViewId());
                headerTopRoundCardView.setId(RenderViewCompat.generateViewId());
                headerTopRoundCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View inflatedView = inflater.inflate(R.layout.g_peek_panel, null, false);
                        final PopupWindow pw = new PopupWindow(inflatedView);

                        pw.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                        pw.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                        pw.showAsDropDown(v);

                        pw.setClippingEnabled(true);

                        zie = 30;
                        final FrameLayout outerLayout = (FrameLayout) pw.getContentView().findViewById(R.id.g_header_peek_peeklayout_optroundcardview);
                        outerLayout.setClipChildren(true);
                        final int[] destinedHeight = new int[1];
                        new Thread() {
                            public void run() {
                                super.run();
                                while (outerLayout.getMeasuredHeight() == 0) {
                                    try {
                                        sleep(20);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                destinedHeight[0] = outerLayout.getMeasuredHeight();

                                Log.i(TAG, "Meaured height: " + destinedHeight[0]);
                                final int destinedWidth = pw.getWidth();

                                ((Activity) v.getContext()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        pw.setHeight(zie);
                                        pw.update();

                                    }
                                });
                                final int increments = destinedHeight[0] / 100;

                                new Thread() {
                                    public void run() {
                                        super.run();
                                        while (zie < destinedHeight[0]) {
                                            try {
                                                Thread.sleep(300);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            if(zie == 30)
                                            ((Activity) v.getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    outerLayout.setVisibility(View.VISIBLE);
                                                }});
                                            zie += increments;

                                            ((Activity) v.getContext()).runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    pw.update(destinedWidth, zie);
                                                }
                                            });
                                        }
                                        //pw.update(vWidth, zie);

                                    }
                                }.start();
                            }
                        }.start();

                        //PopupWindowCompat.showAsDropDown(pw, v, 0, 0, Gravity.BOTTOM);

                        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        /*pw.setContentView();
                        PopupWindowCompat.setOverlapAnchor(pw, false);
                        pw.showAsDropDown(headerTopRoundCardView);*/

                    }
                });
                //autoScaleTextView.setTextColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));
                //((PercentRelativeLayout.LayoutParams) peekPanelRoundCardView.getLayoutParams()).addRule(RelativeLayout.BELOW, headerTopRoundCardView.getId());
                /*if (peekPanel != null) {
                    peekPanel.updateBaseAttributes(autoScaleTextView);
                    View.OnClickListener peekPanelOnClick = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ViewGroup.MarginLayoutParams layoutParams = (OptRoundCardView.MarginLayoutParams) peekPanelRoundCardView.getLayoutParams();
                            if (autoScaleTextView.getVisibility() == View.VISIBLE) {
                                layoutParams.topMargin = (int) autoScaleTextView.getTag(R.id.peek_textview_top_margin);
                                autoScaleTextView.setVisibility(View.GONE);
                            } else {
                                autoScaleTextView.setTag(R.id.peek_textview_top_margin, layoutParams.topMargin);
                                layoutParams.topMargin = 0;
                                autoScaleTextView.setVisibility(View.VISIBLE);

                            }
                        }
                    };
                    peekPanelRoundCardView.setOnClickListener(peekPanelOnClick);
                    headerTopRoundCardView.setOnClickListener(peekPanelOnClick);
                }
                if (autoScaleTextView != null)
                    autoScaleTextView.setVisibility(View.GONE);
                    */
                break;
            case straight:
                tempRoot = inflater.inflate(R.layout.g_header_straight, viewGroup);
                break;
            case clear:
                tempRoot = inflater.inflate(R.layout.g_header_clear, viewGroup);
                break;
            case plain:
                tempRoot = inflater.inflate(R.layout.g_header_plain, viewGroup);
                break;
            default:
                tempRoot = inflater.inflate(R.layout.g_header_default, viewGroup);
//                OptRoundCardView optRoundCardView = (OptRoundCardView) tempRoot.findViewById(R.id.g_header_default_outerlayout_optroundcardview);
//
//                    optRoundCardView.showLeftEdgeShadow(false);

                if (number != null) {
                    number.defaultColor(position);
                    number.updateBaseAttributes((AutoScaleTextView) tempRoot.findViewById(R.id.g_header_default_number_textview));
                }
                //setUpNumberTextView((AutoScaleTextView) tempRoot.findViewById(R.id.g_header_default_number_textview), position);
                break;

        }
        tempRoot.setId(RenderViewCompat.generateViewId());

        updateStandardRoots(tempRoot, position);

        return tempRoot.getId();
    }

    private void updateStandardRoots(View tempRoot, int position) {

        if (tempRoot != null) {

            TextView headerTextView = (TextView) tempRoot.findViewById(R.id.g_header_header_textview);
            TextView subHeaderTextView = (TextView) tempRoot.findViewById(R.id.g_header_subheader_textview);

            boolean removeLayoutBelow = false;
            if (heading != null) {
                heading.defaultColor(position);
                heading.width = null;

                heading.updateBaseAttributes(headerTextView);

            } else {
                if (headerTextView != null)
                    headerTextView.setVisibility(View.GONE);
            }
            if (subheading != null) {
                subheading.defaultColor(position);
                subheading.width = null;
                if (subheading.y != null || heading == null && subHeaderTextView.getLayoutParams() instanceof PercentRelativeLayout.LayoutParams)
                    ((PercentRelativeLayout.LayoutParams) subHeaderTextView.getLayoutParams()).addRule(RelativeLayout.BELOW, -1);

                subheading.updateBaseAttributes(subHeaderTextView);
            } else {
                if (subHeaderTextView != null)
                    subHeaderTextView.setVisibility(View.GONE);
            }

        }

    }

    public enum HeadingMode {
        peek, straight, clear, plain, none
    }

}
