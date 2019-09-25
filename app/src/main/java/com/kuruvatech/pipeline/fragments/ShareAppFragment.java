package com.kuruvatech.pipeline.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.kuruvatech.pipeline.R;
import com.kuruvatech.pipeline.utils.Constants;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kuruvatech.pipeline.R;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class ShareAppFragment extends Fragment {

    Button btnshareApp;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_share_app, container, false);
//        ((MainActivity) getActivity())
//                .setActionBarTitle("Invite Friends");
        btnshareApp= (Button) view.findViewById(R.id.invite_button);
        btnshareApp.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.INVITE_SUBJECT);
                sendIntent.putExtra(Intent.EXTRA_TEXT, Constants.INVITE_TEXT);
                startActivity(Intent.createChooser(sendIntent, "Share link!"));
                }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
    }
}
