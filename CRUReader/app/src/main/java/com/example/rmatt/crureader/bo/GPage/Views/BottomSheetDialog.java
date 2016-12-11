package com.example.rmatt.crureader.bo.GPage.Views;

/**
 * Created by rmatt on 12/11/2016.
 */

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

import com.example.rmatt.crureader.R;

public class BottomSheetDialog extends BottomSheetDialogFragment {



    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);

        //View v = inflater.inflate(R.layout.g_followupmodal, viewGroup);
        //View holder = mModal.render(getContext(), null, false);
        //if (holder != null) {
            dialog.setContentView(R.layout.g_followupmodal);
            //dialog.setContentView(holder.mRoot);
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(@NonNull final DialogInterface d) {
                    if (d instanceof Dialog) {
                        final View bottomSheet =
                                ((Dialog) d).findViewById(android.support.design.R.id.design_bottom_sheet);
                        if (bottomSheet != null) {
                            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                        }
                    }
                }
            });
        //}
    //else {
      //      dismissAllowingStateLoss();
    //    }

        return dialog;
    }



}
