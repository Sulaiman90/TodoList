package com.ms.favtodo.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ms.favtodo.activity.NewTask;
import com.ms.favtodo.R;


public class AlertDialogFragment extends DialogFragment{

    private static final String TAG = "AlertDialogFragment";

    public static AlertDialogFragment newInstance() {
        return new AlertDialogFragment();
    }

    // Build AlertDialog using AlertDialog.Builder
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.delete_alert))
                // User cannot dismiss dialog by hitting back button
                .setCancelable(false)
                // Set up No Button
                .setNegativeButton(getResources().getString(R.string.no_alert),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                ((NewTask) getActivity()).continueDelete(false);
                            }
                        })
                // Set up Yes Button
                .setPositiveButton(getResources().getString(R.string.yes_alert),
                        new DialogInterface.OnClickListener() {
                            public void onClick(
                                    final DialogInterface dialog, int id) {
                                        ((NewTask) getActivity()).continueDelete(true);
                                    }
                        })
                .create();
    }
}
