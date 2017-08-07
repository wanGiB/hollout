package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.utils.ApiUtils;
import com.wan.hollout.utils.AppKeys;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class FeedFragment extends Fragment {

    private List<JSONObject> blogPosts = new ArrayList<>();

    @BindView(R.id.feed_recycler_view)
    RecyclerView feedRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View feedView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, feedView);
        return feedView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ApiUtils.fetchBlogPosts(AppKeys.GENERAL_BLOG_ID, null, new DoneCallback<List<JSONObject>>() {
            @Override
            public void done(List<JSONObject> result, Exception e) {
                feedRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        });
    }

}
