package com.kuruvatech.pipeline.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.kuruvatech.pipeline.App;
import com.kuruvatech.pipeline.MainActivity;
import com.kuruvatech.pipeline.R;
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
import java.util.Locale;

/**
 * Created by dayas on 02-12-2017.
 */

public class Settingfragment extends Fragment {
    Switch switchLanguage;
    private static boolean isEnglish = true;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settinglayout, container, false);
//        ((MainActivity) getActivity())
//                .setActionBarTitle("Invite Friends");
        switchLanguage = (Switch) view.findViewById(R.id.language);
        switchLanguage.setChecked(!isEnglish);
        switchLanguage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {


                   // ((App) getActivity().getApplication()).setLocale(new Locale("kn"));
                    ((App) getActivity().getApplication()).setLocale(new Locale("kn"));
                    isEnglish=false;
                } else {
                    isEnglish=true;
                    ((App) getActivity().getApplication()).setLocale(new Locale("en"));

                }
                refreshUI();
            }
        });
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.titletext));
        return view;
    }
    private ProgressDialog mProgressDialog;

    public void createProgressBar() {

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("Signing........");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
    }

    private void refreshUI() {
       // mProgressDialog.show();
        getActivity().recreate();
       // mProgressDialog.hide();
    }


    //    public void setLocale(String lang) { //call this in onCreate()
//        Locale myLocale = new Locale(lang);
//        Resources res = getResources();
//        DisplayMetrics dm = res.getDisplayMetrics();
//        Configuration conf = res.getConfiguration();
//        conf.locale = myLocale;
//        res.updateConfiguration(conf, dm);
//
//    }
    @Override
    public void onResume() {
        super.onResume();
    }
}
