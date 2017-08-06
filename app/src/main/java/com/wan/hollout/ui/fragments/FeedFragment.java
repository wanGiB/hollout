package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wan.hollout.R;

/**
 * @author Wan Clem
 */

public class FeedFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View feedView = inflater.inflate(R.layout.fragment_feed,container,false);
        return feedView;
    }
}
