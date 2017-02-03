package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

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
    boolean isPopupShowing = false;
    private int zie = 30;

    public int render(LayoutInflater inflater, final ViewGroup viewGroup, final int position) {
        final Context context = viewGroup.getContext();

        // force a rendering mode based on the presence of a peek panel
        if (peekPanel != null) {
            mode = HeadingMode.peek;
        } else if (mode == HeadingMode.peek && peekPanel == null) {
            mode = HeadingMode.straight;
        } else if (mode == null) {
            mode = HeadingMode.none;
        }

        View tempRoot = null;
        switch (mode) {
            case peek:
                tempRoot = inflater.inflate(R.layout.g_header_peak, viewGroup);
                autoTextResizeHeader = true;

                //TODO: revert this back to original approach
                if (RenderViewCompat.SDK_LOLLIPOP) {
                    Log.i(TAG, "SDK Kit Kat or higher");
                    final OptRoundCardView headerTopRoundCardView = (OptRoundCardView) tempRoot.findViewById(R.id.g_header_peek_outerlayout_optroundcardview);
                    final OptRoundCardView peekPanelRoundCardView = (OptRoundCardView) tempRoot.findViewById(R.id.g_header_peek_peeklayout_optroundcardview);
                    final AutoScaleTextView autoScaleTextView = (AutoScaleTextView) tempRoot.findViewById(R.id.g_header_peak_peak_textview);
                    final TextView peekCliffTextView = (TextView)tempRoot.findViewById(R.id.g_header_peak_cliff);

                /*LayoutTransition layoutTransition = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                    layoutTransition = peekPanelRoundCardView.getLayoutTransition();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                }*/

                    autoScaleTextView.setId(RenderViewCompat.generateViewId());
                    peekPanelRoundCardView.setId(RenderViewCompat.generateViewId());
                    headerTopRoundCardView.setId(RenderViewCompat.generateViewId());
                    autoScaleTextView.setTextColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));
                    ((PercentRelativeLayout.LayoutParams) peekPanelRoundCardView.getLayoutParams()).addRule(RelativeLayout.BELOW, headerTopRoundCardView.getId());
                    if (peekPanel != null) {
                        peekPanel.updateBaseAttributes(autoScaleTextView);
                        View.OnClickListener peekPanelOnClick = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ViewGroup.MarginLayoutParams layoutParams = (OptRoundCardView.MarginLayoutParams) peekPanelRoundCardView.getLayoutParams();
                                if (autoScaleTextView.getVisibility() == View.VISIBLE) {
                                    layoutParams.topMargin = (int) autoScaleTextView.getTag(R.id.peek_textview_top_margin);
                                    autoScaleTextView.setVisibility(View.GONE);
                                    peekCliffTextView.setVisibility(View.VISIBLE);
                                } else {
                                    autoScaleTextView.setTag(R.id.peek_textview_top_margin, layoutParams.topMargin);
                                    layoutParams.topMargin = 0;
                                    peekCliffTextView.setVisibility(View.GONE);
                                    autoScaleTextView.setVisibility(View.VISIBLE);


                                }
                            }
                        };
                        peekPanelRoundCardView.setOnClickListener(peekPanelOnClick);
                        headerTopRoundCardView.setOnClickListener(peekPanelOnClick);
                    }
                    if (autoScaleTextView != null)
                        autoScaleTextView.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "SDK less than Kit Kat");
                    final FrameLayout headerTopRoundCardView = (FrameLayout) tempRoot.findViewById(R.id.g_header_peek_outerlayout_optroundcardview);
                    headerTopRoundCardView.setId(RenderViewCompat.generateViewId());
                    View peekView = inflater.inflate(R.layout.g_peek_panel_cliff, viewGroup);
                    final FrameLayout fl = (FrameLayout) peekView.findViewById(R.id.g_header_peek_cliff_framelayout);

                    ((RelativeLayout.LayoutParams) fl.getLayoutParams()).addRule(RelativeLayout.BELOW, headerTopRoundCardView.getId());

                    headerTopRoundCardView.setOnClickListener(new View.OnClickListener() {
                                                                  @Override
                                                                  public void onClick(final View view) {

                                                                          LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                                          View inflatedView = inflater.inflate(R.layout.g_peek_panel, null, false);
                                                                          final FrameLayout outerLayout = (FrameLayout) inflatedView.findViewById(R.id.g_header_peek_peeklayout_optroundcardview);


                                                                          final AutoScaleTextView autoScaleTextView = (AutoScaleTextView) inflatedView.findViewById(R.id.g_header_peak_peak_textview);
                                                                          autoScaleTextView.setTextColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));
                                                                          peekPanel.updateBaseAttributes(autoScaleTextView);
                                                                          final PopupWindow pw = new PopupWindow(inflatedView);
                                                                          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                                                                              pw.setAttachedInDecor(true);
                                                                          }
                                                                          pw.setTouchInterceptor(new View.OnTouchListener() {
                                                                              @Override
                                                                              public boolean onTouch(View v, MotionEvent event) {

                                                                                  pw.dismiss();

                                                                                  v.postDelayed(new Runnable() {
                                                                                      @Override
                                                                                      public void run() {
                                                                                          view.setClickable(true);
                                                                                      }


                                                                                  }, 15);

                                                                                  Log.i(TAG, "OnTouch Interceptor");
                                                                                  return true;
                                                                              }
                                                                          });
                                                                          pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                                                              @Override
                                                                              public void onDismiss() {
                                                                                  Log.i(TAG, "On Dismiss Listener is called");

                                                                              }
                                                                          });
                                                                          pw.setBackgroundDrawable(new BitmapDrawable());
                                                                          pw.setWidth(fl.getWidth());
                                                                          pw.setFocusable(false);
                                                                          pw.setOutsideTouchable(true);
                                                                          pw.setTouchable(true);
                                                                          pw.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                                                                          pw.showAsDropDown(view);
                                                                          view.setClickable(false);
                                                                          pw.setClippingEnabled(true);


                                                                  }
                                                              }

                    );
                }

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

                if (number != null) {
                    number.defaultColor(position);
                    number.updateBaseAttributes((AutoScaleTextView) tempRoot.findViewById(R.id.g_header_default_number_textview));
                }
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
