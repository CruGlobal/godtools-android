package org.keynote.godtools.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.service.BackgroundService;

public class AccessCodeDialogFragment extends DialogFragment
{

    private AccessCodeDialogListener mListener;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mListener = (AccessCodeDialogListener) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.fragment_access_code, null);
        final EditText etAccessCode = (EditText) root.findViewById(R.id.etAccessCode);

        etAccessCode.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND)
                {
                    handleAction(true, v.getText().toString());
                    dismiss();
                    handled = true;
                }
                return handled;
            }
        });

        String title = getString(R.string.dialog_access_code_title);
        String positive = getString(R.string.send);
        String negative = getString(R.string.cancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(root)
                .setPositiveButton(positive, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // check if empty
                        String code = etAccessCode.getText().toString();
                        handleAction(true, code);
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        handleAction(false, null);
                        dismiss();
                    }
                });

        return builder.create();
    }

    private void handleAction(Boolean positive, String accessCode)
    {
        // positive button is pushed
        if (positive)
        {
            // code field is empty
            if (accessCode.isEmpty())
            {
                mListener.onAccessDialogClick(false);
                Toast.makeText(getActivity(), getString(R.string.invalid_code), Toast.LENGTH_SHORT).show();
            }
            else
            {
                // authenticate code
                mListener.onAccessDialogClick(true);
                BackgroundService.authenticateAccessCode(getActivity(), accessCode);
            }
        }
        else
        {
            mListener.onAccessDialogClick(false);
        }
    }

    public interface AccessCodeDialogListener
    {
        void onAccessDialogClick(boolean success);
    }
}
