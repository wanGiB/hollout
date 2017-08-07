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
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.ui.adapters.FeedAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.ApiUtils;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
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
    private FeedAdapter feedAdapter;

    @BindView(R.id.feed_recycler_view)
    RecyclerView feedRecyclerView;

    @BindView(R.id.feedContentFlipper)
    ViewFlipper feedContentFlipper;

    @BindView(R.id.error_message)
    HolloutTextView errorMessageView;

    @BindView(R.id.retry)
    HolloutTextView retryButton;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private View loadingFooterView;
    private String TAG = "FeedFragment";

    private boolean loadingFeed = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View feedView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, feedView);
        return feedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
            setupAdapter();
            fetchBlogPosts();
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (!loadingFeed) {
                        fetchBlogPosts();
                    }
                }
            });
            retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.toggleFlipperState(feedContentFlipper, 0);
                    fetchBlogPosts();
                }
            });
        }
    }

    private void fetchBlogPosts() {
        loadingFeed = true;
        ApiUtils.fetchBlogPosts(AppKeys.GENERAL_BLOG_ID, null, new DoneCallback<List<JSONObject>>() {
            @Override
            public void done(final List<JSONObject> result, final Exception e) {
                feedRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (e == null) {
                            if (result != null) {
                                if (!result.isEmpty()) {
                                    UiUtils.toggleFlipperState(feedContentFlipper, 2);
                                    populateBlogs(result);
                                }
                            }
                        } else {
                            if (blogPosts.isEmpty()) {
                                UiUtils.toggleFlipperState(feedContentFlipper, 1);
                                errorMessageView.setText(e.getMessage());
                            } else {
                                UiUtils.showSafeToast("Error fetching more feeds. Please review your data connection");
                            }
                        }
                        loadingFeed = false;
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void populateBlogs(List<JSONObject> result) {
        for (JSONObject jsonObject : result) {
            if (!blogPosts.contains(jsonObject)) {
                blogPosts.add(jsonObject);
                try {
                    feedAdapter.notifyItemInserted(blogPosts.size() - 1);
                } catch (IllegalStateException ie) {
                    HolloutLogger.d(TAG, ie.getMessage());
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    public void setupAdapter() {
        feedAdapter = new FeedAdapter(getActivity(), blogPosts);
        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(feedAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        feedRecyclerView.setLayoutManager(linearLayoutManager);
        feedRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);

        loadingFooterView = getActivity().getLayoutInflater().inflate(R.layout.loading_footer, null);
        headerAndFooterRecyclerViewAdapter.addFooterView(loadingFooterView);
        UiUtils.showView(loadingFooterView, false);

        feedRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                UiUtils.showView(loadingFooterView, true);
                if (!blogPosts.isEmpty()) {
                    JSONObject lastBlogPost = blogPosts.get(blogPosts.size() - 1);
                    if (lastBlogPost != null) {
                        String nextPageToken = lastBlogPost.optString(AppConstants.NEXT_PAGE_TOKEN);
                        if (StringUtils.isNotEmpty(nextPageToken)) {
                            ApiUtils.fetchBlogPosts(AppKeys.GENERAL_BLOG_ID, nextPageToken,
                                    new DoneCallback<List<JSONObject>>() {
                                        @Override
                                        public void done(final List<JSONObject> result, final Exception e) {
                                            feedRecyclerView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (e == null) {
                                                        if (result != null) {
                                                            if (!result.isEmpty()) {
                                                                populateBlogs(result);
                                                            }
                                                        }
                                                    }
                                                    UiUtils.showView(loadingFooterView, false);
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                }
            }
        });
    }

}
