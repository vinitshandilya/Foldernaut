package com.vinit.foldernaut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinit.foldernaut.objects.YesNoDialogClickListener;

public class YesNoDialog extends android.support.v4.app.DialogFragment{

    private YesNoDialogClickListener yesNoDialogClickListener;

    @NonNull
    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title_key");
        String body = getArguments().getString("body_key");

        try {
            yesNoDialogClickListener = (YesNoDialogClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //LayoutInflater inflater = getActivity().getLayoutInflater();
        //builder.setView(inflater.inflate(R.layout.yes_no_dialog, null));
        builder.setTitle(title)
                .setMessage(body)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                yesNoDialogClickListener.onYesClick();

            }
        })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                yesNoDialogClickListener.onNoClick();

            }
        });

        AlertDialog dialog = builder.create();



        return dialog;
    }
}
