package org.keynote.godtools.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.keynote.godtools.android.R;

public class AlertDialogFragment extends DialogFragment{

    private static final String ARGS_TAG = "tag";
    private static final String ARGS_TITLE = "title";
    private static final String ARGS_MESSAGE = "message";
    private static final String ARGS_POSITIVE = "positive";
    private static final String ARGS_NEGATIVE = "negative";

    public interface OnDialogClickListener{
        public void onDialogClick(boolean positive, String tag);
    }

    private OnDialogClickListener mListener;

    public static DialogFragment newInstance(String title, String message, String tag){
        DialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TAG, tag);
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        frag.setCancelable(false);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnDialogClickListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String tag = getArguments().getString(ARGS_TAG);
        String title = getArguments().getString(ARGS_TITLE);
        String body = getArguments().getString(ARGS_MESSAGE);
        String positive = getString(R.string.yes);
        String negative = getString(R.string.no);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(positive ,new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogClick(true, tag);
                    }
                })

                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogClick(false, tag);
                    }
                });


        return builder.create();
    }
}
