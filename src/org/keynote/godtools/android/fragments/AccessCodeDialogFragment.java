package org.keynote.godtools.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.keynote.godtools.android.R;

public class AccessCodeDialogFragment extends DialogFragment {

    public interface AccessCodeDialogListener {
        public void onAccessDialogClick(boolean positive, String accessCode);
    }

    private AccessCodeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (AccessCodeDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.fragment_access_code, null);
        final EditText etAccessCode = (EditText) root.findViewById(R.id.etAccessCode);

        String title = getString(R.string.dialog_access_code_title);
        String positive = getString(R.string.send);
        String negative = getString(R.string.cancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(root)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // check if empty
                        String code = etAccessCode.getText().toString();
                        mListener.onAccessDialogClick(true, code);
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onAccessDialogClick(false, null);
                    }
                });

        return builder.create();
    }
}
