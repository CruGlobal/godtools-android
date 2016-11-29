package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
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
public class GTitle extends Gtapi<OptRoundCardView, ViewGroup> {


    public static final float DEFAULT_RIGHT_MARGIN = .025F;
    private static final float DEFAULT_TOP_MARGIN = .02F;

    public static final float TITLE_ELEVATION = 10;
    public static final float TITLE_CORNER_RADIUS = 10.0F;


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


    public OptRoundCardView render(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        PercentRelativeLayout.LayoutParams layoutParams;
        setDefaultValues();
        OptRoundCardView cv = new OptRoundCardView(context);
        layoutParams = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.WRAP_CONTENT);
        cv.setLayoutParams(layoutParams);
        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
        percentLayoutInfo.topMarginPercent = DEFAULT_TOP_MARGIN;
        /*cv.setPreventCornerOverlap(false);
        cv.setUseCompatPadding(false);
        cv.setContentPadding(0,0,0,0);
        cv.setMeasureAllChildren(false);
        cv.setLayoutParams(layoutParams);*/



        LinearLayout headingLayout = new LinearLayout(context);
        headingLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (number != null && number != "") {
            TextView numberTextView = new TextView(context);
            RenderConstants.setDefaultPadding(numberTextView);
            LinearLayout.LayoutParams numberTextViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            setUpNumberTextView(numberTextView, position);
            headingLayout.addView(numberTextView, numberTextViewLayoutParams);
        }
        TextView headingTextView = heading.render(viewGroup, position);
       // headingTextView.setGravity(Gravity.CENTER_VERTICAL);
        //headingTextView.setSingleLine(false);

        LinearLayout subHeadingAndHeadingLayout = new LinearLayout(context);
        subHeadingAndHeadingLayout.setOrientation(LinearLayout.VERTICAL);



        headingLayout.setGravity(Gravity.CENTER_VERTICAL);


        subHeadingAndHeadingLayout.addView(headingTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (subheading != null) {
            Log.i(TAG, "Subheading != null");
            TextView subheadingTextView = subheading.render(viewGroup, position);
            subHeadingAndHeadingLayout.addView(subheadingTextView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        if (number == null || number == "")
        {
            cv.addView(subHeadingAndHeadingLayout, new OptRoundCardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        else
        {
            headingLayout.addView(subHeadingAndHeadingLayout);
            cv.addView(headingLayout, new OptRoundCardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }


        switch (mode) {
            case peek:
                cv.setCardBackgroundColor(Color.WHITE);

                cv.showCorner(false, true, false, true);
                cv.setRadius(TITLE_CORNER_RADIUS);
                //cv.setShadowPadding(10, 10, 10, 10);
                cv.setCardElevation(TITLE_ELEVATION);
                percentLayoutInfo.rightMarginPercent = DEFAULT_RIGHT_MARGIN;

                //do popout
                break;
            case straight:

                cv.setCardBackgroundColor(Color.WHITE);
                cv.setCardElevation(TITLE_ELEVATION);
                //cv.setShadowPadding(10, 10, 10, 10);

                break;
            case clear:

                Log.i(TAG, "Clear settings");
                cv.setCardBackgroundColor(Color.TRANSPARENT);
                //cv.setShadowPadding(10, 10, 10, 10);

                //cv.setBackgroundColor(Color.TRANSPARENT);
                cv.showEdgeShadow(false, false, false, false);
                cv.showCorner(false, false, false, false);

                break;
            case plain:
            default:
                cv.setCardBackgroundColor(Color.WHITE);
                percentLayoutInfo.rightMarginPercent = DEFAULT_RIGHT_MARGIN;
                //cv.setShadowPadding(10, 10, 10, 10);
                cv.showCorner(false, true, false, true);


                //put back
                cv.setRadius(TITLE_CORNER_RADIUS);

                cv.setCardElevation(TITLE_ELEVATION);
                break;

        }
        return cv;

    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }


    private void setUpNumberTextView(TextView numberTextView, int position) {
        @ColorInt int numberTextColor = heading.color != null ? Color.parseColor(heading.color) : RenderSingleton.getInstance().getPositionGlobalColorAsInt(position);
        numberTextView.setText(number);
        numberTextView.setTextColor(numberTextColor);
        //numberTextView.setTextColor(Color.parseColor((heading.color != null && heading.color.length() > 0) ? heading.color : RenderSingleton.getInstance().getPositionGlobalColorAsString(position)));
        numberTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        numberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(RenderConstants.DEFAULT_NUMBER_TEXT_SIZE));
        RenderConstants.setDefaultPadding(numberTextView);
    }


    public void setDefaultValues() {
        if (mode == null) mode = HeadingMode.plain;
    }


}
