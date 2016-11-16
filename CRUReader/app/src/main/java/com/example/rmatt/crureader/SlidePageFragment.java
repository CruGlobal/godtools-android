package com.example.rmatt.crureader;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.GPage;

/**
 * Created by rmatt on 10/24/2016.
 */
public class SlidePageFragment extends Fragment {


    private static final String ARG_DOCUMENT_ID = "DOCUMENT_ID";
    private static final String ARG_POSITION = "POSITION";
    private static final String TAG = "SlidePageFragment";
    private String mXmlDocumentId;
    private GPage mGPage;
    private int mPosition;

    FrameLayout thisView;

    public static Fragment create(int position, String xmlDocumentId) {
        SlidePageFragment fragment = new SlidePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_ID, xmlDocumentId);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public SlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mXmlDocumentId = getArguments().getString(ARG_DOCUMENT_ID);
        mPosition = getArguments().getInt(ARG_POSITION);

        try {
            Log.i(TAG, "XMLDocument: " + mXmlDocumentId);
            mGPage = XMLUtil.parseGPage(this.getActivity(), mXmlDocumentId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.page, container, false);
        thisView = (FrameLayout) rootView.findViewById(R.id.genericViewGroup);
        return thisView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        PercentRelativeLayout percentRelativeLayout = mGPage.render(thisView);
        thisView.addView(percentRelativeLayout, layoutParams);

    }
}
