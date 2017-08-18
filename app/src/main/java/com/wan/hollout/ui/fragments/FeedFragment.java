package com.wan.hollout.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.ChatRequestView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    @BindView(R.id.content_view)
    LinearLayout parentView;

    @BindView(R.id.chat_request_view)
    ChatRequestView chatRequestView;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView =  inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this,parentView);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        chatRequestView.bindData(getActivity(),parentView,null);
    }
}
