package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentFrameLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Views.RootTextColorTextView;
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


    public static final String TAG = "GTitle";


    public enum HeadingMode {
        peek, straight, clear, plain, none
    }

    @Element(required = false)
    public GBaseTextAttributes heading;

    @Element(required = false)
    public GBaseTextAttributes subheading;

    @Attribute(required = false)
    public HeadingMode mode;

    @Element(required = false)
    public String number;

    @Element(required = false, name = "peekpanel")
    public GBaseTextAttributes peekPanel;

    public OptRoundCardView render(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();


        setDefaultValues();
        LayoutInflater inflaterService = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tempRoot = null;
        switch (mode) {
            case peek:
                tempRoot = inflaterService.inflate(R.layout.g_header_peak, viewGroup);

                peekPanel.updateBaseAttributes(tempRoot.findViewById(R.id.g_header_peak_peak_textview));
                break;
            case straight:
                tempRoot = inflaterService.inflate(R.layout.g_header_straight, viewGroup);
                break;
            case clear:
                tempRoot = inflaterService.inflate(R.layout.g_header_clear, viewGroup);
                break;
            case plain:
                tempRoot = inflaterService.inflate(R.layout.g_header_plain, viewGroup);
                break;
            default:
                tempRoot = inflaterService.inflate(R.layout.g_header_default, viewGroup);
                setUpNumberTextView((RootTextColorTextView) tempRoot.findViewById(R.id.g_header_default_number_textview), position);
                break;

        }

        updateStandardRoots(tempRoot, position);
        return null;

    }

    private void updateStandardRoots(View tempRoot, int position) {
        if (tempRoot != null) {
            if (heading != null) {

                heading.updateBaseAttributes(tempRoot.findViewById(R.id.g_header_header_textview));
            }
            if (subheading != null) {

                subheading.updateBaseAttributes(tempRoot.findViewById(R.id.g_header_subheader_textview));
            }
        }

    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }


    private void setUpNumberTextView(RootTextColorTextView numberTextView, int position) {
        if (number != null) {

            numberTextView.setText(number);
            //numberTextView.setTextColor(numberTextColor);
        } else {
            PercentFrameLayout.LayoutParams pli = (PercentFrameLayout.LayoutParams) numberTextView.getLayoutParams();
            pli.getPercentLayoutInfo().widthPercent = 0;
        }
    }


    public void setDefaultValues() {
        if (mode == null) mode = HeadingMode.none;
    }


    /* Recent clean up.



//    public static final float DEFAULT_RIGHT_MARGIN = .025F;
//    private static final float DEFAULT_TOP_MARGIN = .02F;
//
//    public static final float TITLE_ELEVATION = 20;
//    public static final float TITLE_CORNER_RADIUS = 30.0F;


        //TODO: these properties moved to xml to clean up
        //numberTextView.setTextColor(Color.parseColor((heading.color != null && heading.color.length() > 0) ? heading.color : RenderSingleton.getInstance().getPositionGlobalColorAsString(position)));
//        numberTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
//        numberTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(RenderConstants.DEFAULT_NUMBER_TEXT_SIZE));
//        RenderConstants.setDefaultPadding(numberTextView);

     */


}
