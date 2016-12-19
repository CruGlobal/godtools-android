package org.keynote.godtools.renderer.crureader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

/**
 * Created by rmatt on 10/24/2016.
 */
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
            Log.w(TAG, "XMLDocument: " + mXmlDocumentId);
            mGPage = XMLUtil.parseGPage(this.getActivity(), mXmlDocumentId);


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
        return mGPage.rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        //mGPage.render(thisView, mPosition);


    }
}
