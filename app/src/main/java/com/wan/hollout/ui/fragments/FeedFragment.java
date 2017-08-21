package com.wan.hollout.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.eventbuses.UnreadFeedsBadge;
import com.wan.hollout.ui.adapters.FeedAdapter;
import com.wan.hollout.ui.widgets.ChatRequestsAdapterView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * FeedFragment
 */
public class FeedFragment extends Fragment {

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.feed_recycler_view)
    RecyclerView feedRecyclerView;

    private ParseUser signedInUser;
    private View headerView;
    private ChatRequestsAdapterView chatRequestsAdapterView;

    private List<ParseObject> feeds = new ArrayList<>();

    private FeedAdapter feedAdapter;

    public FeedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, parentView);
        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        initSignedInUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        initSignedInUser();
    }

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = ParseUser.getCurrentUser();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
        initSignedInUser();
        initFeedHeaderView();
        initAdapter();
        fetchFeeds();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchFeeds();
            }
        });
    }

    private void initAdapter() {
        feedAdapter = new FeedAdapter(getActivity(), feeds);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(feedAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        feedRecyclerView.setLayoutManager(linearLayoutManager);
        feedRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);
        RecyclerViewUtils.setHeaderView(feedRecyclerView, headerView);
        UiUtils.showView(headerView, false);
        feedRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!feeds.isEmpty()) {
                    fetchFeedsMeantForCurrentUser(feeds.size() - 1);
                }
            }
        });
    }

    @SuppressLint("InflateParams")
    private void initFeedHeaderView() {
        headerView = getLayoutInflater().inflate(R.layout.feed_header_view, null);
        chatRequestsAdapterView = (ChatRequestsAdapterView) headerView.findViewById(R.id.chat_requests_adapter_view);
    }

    private void fetchFeeds() {
        fetchChatRequests();
    }

    private void fetchChatRequests() {
        if (signedInUser != null) {
            ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.APP_USER_ID));
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null && !objects.isEmpty()) {
                        feeds.add(new ParseObject(AppConstants.HOLLOUT_FEED));
                        feedAdapter.notifyDataSetChanged();
                        UiUtils.toggleFlipperState(contentFlipper, 1);
                        chatRequestsAdapterView.setChatRequests(getActivity(), objects);
                        UiUtils.showView(headerView, true);
                        if (!feeds.isEmpty()) {
                            EventBus.getDefault().post(new UnreadFeedsBadge(feeds.size()));
                        }
                    }
                    fetchFeedsMeantForCurrentUser(0);
                }
            });
        }
    }

    private void fetchFeedsMeantForCurrentUser(final int skip) {
        ParseQuery<ParseObject> nonChatQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        nonChatQuery.whereNotEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
        nonChatQuery.setLimit(100);
        if (skip != 0) {
            nonChatQuery.setSkip(skip);
        }
        nonChatQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (skip == 0) {
                    feeds.clear();
                }
                if (e == null && objects != null && !objects.isEmpty()) {
                    UiUtils.toggleFlipperState(contentFlipper, 1);
                    for (ParseObject feedObject : objects) {
                        if (!feeds.contains(feedObject)) {
                            feeds.add(feedObject);
                        }
                    }
                    feedAdapter.notifyDataSetChanged();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

}
