package org.keynote.godtools.renderer.crureader.bo.GPage.Views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.IDO.IContexual;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

public class BottomSheetDialog extends BottomSheetDialogFragment implements IContexual {

    private static final String ARG_POSITION = "position";
    private static final String ARG_CACHE_ID = "cacheId";
    PercentRelativeLayout prl;
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

        int mCacheId = getArguments().getInt(ARG_CACHE_ID);
        mPosition = getArguments().getInt(ARG_POSITION);
        mGCoordinator = RenderSingleton.getInstance().gPanelHashMap.get(mCacheId);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        PercentRelativeLayout prl = new PercentRelativeLayout(getContext());
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //TODO: fix padding
        marginLayoutParams.topMargin = 30;
        marginLayoutParams.bottomMargin = 30;

        prl.setLayoutParams(marginLayoutParams);
        int viewId = mGCoordinator.render((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), prl, mPosition);

        dialog.setContentView(prl);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(@NonNull final DialogInterface d) {
                if (d instanceof Dialog) {
                    final View bottomSheet =
                            ((Dialog) d).findViewById(android.support.design.R.id.design_bottom_sheet);
                    if (bottomSheet != null) {
                        bottomSheet.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(mPosition));
                        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            }
        });
        //} /*else {
        //   dismissAllowingStateLoss();
        //}

        return dialog;
    }

    @Override
    public FragmentManager getContexualFragmentActivity() {
        return this.getFragmentManager();
    }
}
    /*@Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_bottomsheetdialog, container, false);
        prl = (PercentRelativeLayout)inflatedView.findViewById(R.id.fragment_bottomsheetdialog_outerlayout);


        return inflatedView;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



    }


}*/
