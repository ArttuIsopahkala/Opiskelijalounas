package com.ardeapps.opiskelijalounas.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.ardeapps.opiskelijalounas.AppRes;
import com.ardeapps.opiskelijalounas.R;

public class SendFeedbackDialogFragment extends DialogFragment {

    public interface Listener {
        void onFeedbackSent(String message);
    }

    static Listener mListener = null;

    public static void setListener(Listener l) {
        mListener = l;
    }

    Button sendFeedbackButton;
    Button cancelButton;
    EditText message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.send_feedback_dialog, container, false);
        sendFeedbackButton = (Button) v.findViewById(R.id.sendFeedbackButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);
        message = (EditText) v.findViewById(R.id.message);

        message.requestFocus();
        message.setHorizontallyScrolling(false);
        message.setMaxLines(10);
        message.setText("");

        sendFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!message.getText().toString().equals("")) {
                    AppRes.hideKeyBoard(message);
                    dismiss();
                    String messageText = message.getText().toString();
                    mListener.onFeedbackSent(messageText);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppRes.hideKeyBoard(message);
                dismiss();
            }
        });
        return v;
    }
}
