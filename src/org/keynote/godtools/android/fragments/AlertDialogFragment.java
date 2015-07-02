package org.keynote.godtools.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.keynote.godtools.android.R;

public class AlertDialogFragment extends DialogFragment
{

    private static final String ARGS_TITLE = "title";
    private static final String ARGS_MESSAGE = "message";


    public static DialogFragment newInstance(String title, String message)
    {
        DialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_MESSAGE, message);
        frag.setCancelable(false);
        frag.setArguments(args);

        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {

        String title = getArguments().getString(ARGS_TITLE);
        String body = getArguments().getString(ARGS_MESSAGE);
        String positive = getString(R.string.ok);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(body)
                .setPositiveButton(positive, new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // do nothing
                    }
                });

        return builder.create();
    }
}
