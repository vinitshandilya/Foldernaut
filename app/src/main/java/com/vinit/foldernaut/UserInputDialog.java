package com.vinit.foldernaut;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import com.vinit.foldernaut.objects.UserInputDialogClickListener;

public class UserInputDialog extends DialogFragment {

    private UserInputDialogClickListener userInputDialogClickListener;
    private String newname = "Untitled";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title_key");
        String body = getArguments().getString("body_key");

        try {
            userInputDialogClickListener = (UserInputDialogClickListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final View v = getActivity().getLayoutInflater().inflate(R.layout.user_input_dialog_layout, null);
        final EditText inputTextView = (EditText)v.findViewById(R.id.userinputdialogtext);
        inputTextView.setText(body);
        try {
            inputTextView.setSelection(0, body.indexOf("."));
        } catch (Exception e) {}


        builder.setTitle(title)
                .setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        newname = inputTextView.getText().toString();

                        System.out.println(inputTextView.getText().toString());

                        userInputDialogClickListener.onInputYesClick();

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        userInputDialogClickListener.onInputNoClick();

                    }
                });

        AlertDialog dialog = builder.create();



        return dialog;
    }

    public String getNewname() {
        return newname;
    }
}
