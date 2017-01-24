package org.keynote.godtools.renderer.crureader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import java.io.File;

public class SlidePageFragment extends Fragment {


    private static final String ARG_DOCUMENT_ID = "DOCUMENT_ID";
    private static final String ARG_POSITION = "POSITION";
    private static final String TAG = "SlidePageFragment";
    FrameLayout thisView;
    private String mXmlDocumentId;
    private GPage mGPage;
    private int mPosition;

    public SlidePageFragment() {
    }

    public static Fragment create(int position) {
        SlidePageFragment fragment = new SlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment create(int position, String xmlDocumentId) {
        SlidePageFragment fragment = new SlidePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DOCUMENT_ID, xmlDocumentId);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mXmlDocumentId = getArguments().getString(ARG_DOCUMENT_ID);
        mPosition = getArguments().getInt(ARG_POSITION);

        try {
            if(mXmlDocumentId != null && !mXmlDocumentId.equalsIgnoreCase("")) {
                Log.w(TAG, "XMLDocument: " + mXmlDocumentId);
                mGPage = XMLUtil.parseGPage(new File(mXmlDocumentId));
            }
            else
            {
                mGPage = RenderSingleton.getInstance().getPages(mPosition);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RenderSingleton.getInstance().addGlobalColor(mPosition, mGPage.getBackgroundColor());



        int viewId = mGPage.render(inflater, container, mPosition); //inflater.inflate(R.layout.page, container, false);

        //thisView = (FrameLayout) rootView.findViewById(R.id.gpage_root);
        return new TextView(inflater.getContext());
    }

}
