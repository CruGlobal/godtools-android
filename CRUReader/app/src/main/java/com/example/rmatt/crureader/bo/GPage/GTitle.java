package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.Gtapi;
import com.github.captain_miao.optroundcardview.OptRoundCardView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/18/2016.
 */

@Root(name = "title")
public class GTitle extends Gtapi implements IRender {


    public static final float DEFAULT_RIGHT_MARGIN = .025F;
    private static final float DEFAULT_TOP_MARGIN = .02F;

    public static final float TITLE_ELEVATION = 60.0F;
    public static final float TITLE_CORNER_RADIUS = 50.0F;


    public static final String TAG = "GTitle";


    public enum HeadingMode {
        peek, straight, clear, plain
    }

    @Element
    public GHeading heading;

    @Element(required = false)
    public GSubheading subheading;

    @Attribute(required = false)
    public HeadingMode mode;

    @Element(required = false)
    public String number;

    @Attribute(required = false)
    public int h;


    public ViewGroup render(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        PercentRelativeLayout.LayoutParams layoutParams;
        setDefaultValues();
        OptRoundCardView cv = new OptRoundCardView(context);

        LinearLayout headingLayout = new LinearLayout(context);
        headingLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView numberTextView = new TextView(context);
        RenderConstants.setDefaultPadding(numberTextView);
        LinearLayout.LayoutParams numberTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setUpNumberTextView(numberTextView);


        TextView headingTextView = heading.render(viewGroup);
        headingTextView.setGravity(Gravity.CENTER_VERTICAL);
        //headingTextView.setSingleLine(false);

        LinearLayout.LayoutParams headerTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);


        headingLayout.addView(numberTextView, numberTextViewLayoutParams);


        LinearLayout.LayoutParams headerLinearLayoutLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams subheaderTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout subHeadingAndHeadingLayout = new LinearLayout(context);
        subHeadingAndHeadingLayout.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.WRAP_CONTENT);

        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
        percentLayoutInfo.topMarginPercent = DEFAULT_TOP_MARGIN;


        switch (mode) {
            case peek:
                cv.setBackgroundColor(Color.WHITE);

                cv.showCorner(false, true, false, true);
                cv.setRadius(TITLE_CORNER_RADIUS);
                cv.setShadowPadding(10, 10, 10, 10);
                cv.setCardElevation(TITLE_ELEVATION);
                percentLayoutInfo.rightMarginPercent = DEFAULT_RIGHT_MARGIN;

                //do popout
                break;
            case straight:

                cv.setBackgroundColor(Color.WHITE);
                cv.setCardElevation(TITLE_ELEVATION);
                cv.setShadowPadding(10, 10, 10, 10);

                break;
            case clear:

                Log.i(TAG, "Clear settings");
                cv.setBackgroundColor(Color.TRANSPARENT);
                cv.setShadowPadding(10, 10, 10, 10);

                //cv.setBackgroundColor(Color.TRANSPARENT);
                //cv.showEdgeShadow(false, false, false, false);
                //cv.showCorner(false, false, false, false);

                break;
            case plain:
            default:
                cv.setBackgroundColor(Color.WHITE);
                percentLayoutInfo.rightMarginPercent = DEFAULT_RIGHT_MARGIN;
                cv.setShadowPadding(10, 10, 10, 10);
                cv.showCorner(false, true, false, true);
                cv.setRadius(TITLE_CORNER_RADIUS);
                cv.setCardElevation(TITLE_ELEVATION);
                break;

        }
        headingLayout.setGravity(Gravity.CENTER_VERTICAL);


        subHeadingAndHeadingLayout.addView(headingTextView, headerTextViewLayoutParams);

        if (subheading != null) {
            Log.i(TAG, "Subheading != null");
            TextView subheadingTextView = subheading.render(viewGroup);
            subHeadingAndHeadingLayout.addView(subheadingTextView, subheaderTextViewLayoutParams);
        }

        headingLayout.addView(subHeadingAndHeadingLayout);
        cv.addView(headingLayout);

        cv.setLayoutParams(layoutParams);

        return cv;

    }



    private void setUpNumberTextView(TextView numberTextView) {

        numberTextView.setText(number);
        numberTextView.setTextColor(Color.parseColor(heading.color != null ? heading.color : RenderSingleton.getInstance().globalColor));
        numberTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        numberTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, RenderConstants.DEFAULT_NUMBER_TEXT_SIZE);
        RenderConstants.setDefaultPadding(numberTextView);
    }


    public void setDefaultValues() {
        if (mode == null) mode = HeadingMode.plain;
    }


}
