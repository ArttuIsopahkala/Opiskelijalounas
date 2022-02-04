package com.ardeapps.opiskelijalounas.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ardeapps.opiskelijalounas.R;

/**
 * Created by Arttu on 29.11.2015.
 */
public class InfoDialogFragment extends DialogFragment {

    TextView info_text;
    TextView title;
    Button ok_button;
    String title_text;
    String desc_text;

    public static InfoDialogFragment newInstance(String title, String desc) {
        InfoDialogFragment f = new InfoDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("desc", desc);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title_text = getArguments().getString("title", "");
        desc_text = getArguments().getString("desc", "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_dialog, container);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        title = (TextView) v.findViewById(R.id.title);
        info_text = (TextView) v.findViewById(R.id.info_text);

        title.setText(title_text);
        info_text.setText(desc_text);

        ok_button = (Button) v.findViewById(R.id.btn_ok);
        ok_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return v;
    }
}
