package com.wan.hollout.ui.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends Fragment {

    private ParseUser signedInUser;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View parentView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, parentView);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        signedInUser = ParseUser.getCurrentUser();
        super.onActivityCreated(savedInstanceState);
    }

    private void fetchFeedsMeantForCurrentUse() {

        ParseQuery<ParseObject> feedsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);

        if (signedInUser != null) {
            feedsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_HYPHENATED_ID, signedInUser.getUsername());
        }
        feedsQuery.include(AppConstants.FEED_CREATOR);

    }

}
