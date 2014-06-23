package org.keynote.godtools.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

public class LanguageDialogFragment extends DialogFragment {

    private static final String ARGS_LANGUAGE_NAME = "name";
    private static final String ARGS_LANGUAGE_CODE = "code";

    public interface OnLanguageChangedListener {
        public void onLanguageChanged(String name, String code);
    }

    private OnLanguageChangedListener mListener;

    public static DialogFragment newInstance(String langName, String langCode) {
        DialogFragment frag = new LanguageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_LANGUAGE_NAME, langName);
        args.putString(ARGS_LANGUAGE_CODE, langCode);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnLanguageChangedListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String name = getArguments().getString(ARGS_LANGUAGE_NAME);
        final String code = getArguments().getString(ARGS_LANGUAGE_CODE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("Language Settings")
                .setMessage(String.format("Would you like to set %s as the default language?", name))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onLanguageChanged(name, code);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
                    }
                });


        return builder.create();
    }
}
