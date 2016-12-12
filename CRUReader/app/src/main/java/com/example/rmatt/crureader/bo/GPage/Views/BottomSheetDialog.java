package com.example.rmatt.crureader.bo.GPage.Views;

/**
 * Created by rmatt on 12/11/2016.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.percent.PercentRelativeLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public class BottomSheetDialog extends BottomSheetDialogFragment {


    private static final String ARG_POSITION = "position";
    private static final String ARG_CACHE_ID = "cacheId";

    private int mCacheId;
    private int mPosition;
    private GCoordinator mGCoordinator;

    public static BottomSheetDialog create(int position, int cacheId) {
        BottomSheetDialog fragment = new BottomSheetDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_CACHE_ID, cacheId);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCacheId = getArguments().getInt(ARG_CACHE_ID);
        mPosition = getArguments().getInt(ARG_POSITION);
        mGCoordinator = RenderSingleton.getInstance().gPanelHashMap.get(mCacheId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        PercentRelativeLayout prl = new PercentRelativeLayout(this.getContext());
        int v = mGCoordinator.render(inflater, prl, mPosition);

        return prl;

    }


}
