package com.example.rmatt.crureader.bo.GPage.Views;

/**
 * Created by rmatt on 12/11/2016.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.IDO.IContexual;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public class BottomSheetDialog extends BottomSheetDialogFragment implements IContexual {


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
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
//        final Dialog dialog = super.onCreateDialog(savedInstanceState);
//
//        //assert mModal != null;
//       // final GtFollowupModal.ViewHolder holder = mModal.render(getContext(), null, false);
//       // if (holder != null) {
//            dialog.setContentView(holder.mRoot);
//            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//                @Override
//                public void onShow(@NonNull final DialogInterface d) {
//                    if (d instanceof Dialog) {
//                        final View bottomSheet =
//                                ((Dialog) d).findViewById(android.support.design.R.id.design_bottom_sheet);
//                        if (bottomSheet != null) {
//                            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
//                        }
//                    }
//                }
//            });
//        } else {
//            dismissAllowingStateLoss();
//        }
//
//        return dialog;
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        PercentRelativeLayout prl = new PercentRelativeLayout(this.getContext());

        /*this.getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                if(dialogInterface instanceof Dialog) {
                    final View bottomSheet =
                            ((Dialog) dialogInterface).findViewById(android.support.design.R.id.design_bottom_sheet);


                    if (bottomSheet != null) {
                        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }

            }
        });*/
        prl.setLayoutParams(new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        prl.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(mPosition));
        int viewId = mGCoordinator.render(inflater, prl, mPosition);

        return prl;

    }


    @Override
    public FragmentManager getContexualFragmentActivity() {
        return this.getFragmentManager();
    }
}
