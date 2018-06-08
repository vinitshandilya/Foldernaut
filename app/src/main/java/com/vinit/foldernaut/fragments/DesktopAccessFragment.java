package com.vinit.foldernaut.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vinit.foldernaut.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DesktopAccessFragment extends Fragment {


    public DesktopAccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_desktop_access, container, false);
    }

}
