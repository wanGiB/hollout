package com.wan.hollout.ui.fragments;

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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.ui.adapters.ConversationsAdapter;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ConversationsFragment extends Fragment {

    @BindView(R.id.conversations_recycler_view)
    RecyclerView conversationsRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.no_feed_view)
    View noFeedView;

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    private ConversationsAdapter conversationsAdapter;
    private List<ParseObject> conversations = new ArrayList<>();
    private ParseUser signedInUser;

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = ParseUser.getCurrentUser();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSignedInUser();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View peopleView = inflater.inflate(R.layout.fragment_chats, container, false);
        ButterKnife.bind(this, peopleView);
        return peopleView;
    }

    private void setupAdapter() {
        conversationsAdapter = new ConversationsAdapter(getActivity(), conversations);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        conversationsRecyclerView.setLayoutManager(linearLayoutManager);
        conversationsRecyclerView.setAdapter(conversationsAdapter);
        conversationsRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!conversations.isEmpty()) {
                    fetchConversations(conversations.size() - 1);
                }
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        UiUtils.setUpRefreshColorSchemes(getActivity(), swipeRefreshLayout);
        checkAndRegEventBus();
        setupAdapter();
        fetchConversations(0);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchConversations(0);
            }
        });
    }

    private void fetchConversations(int skip) {
        ParseQuery<ParseUser> conversationsQuery = ParseUser.getQuery();
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            conversationsQuery.whereContainedIn(AppConstants.OBJECT_ID, signedInUserChats);
            conversationsQuery.setLimit(100);
            if (skip != 0) {
                conversationsQuery.setSkip(skip);
            }
            conversationsQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (e == null) {
                        if (objects != null && !objects.isEmpty()) {
                            if (!conversations.containsAll(objects)) {
                                conversations.addAll(objects);
                            }
                            sortConversations();
                            conversationsAdapter.notifyDataSetChanged();
                        }
                        invalidateEmptyView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private void sortConversations() {
        Collections.sort(conversations, new ConversationsComparator());
    }

    private void invalidateEmptyView() {
        UiUtils.toggleFlipperState(contentFlipper, conversations.isEmpty() ? 0 : 1);
    }

    @Override
    public void onResume() {
        super.onResume();
        initSignedInUser();
        checkAndRegEventBus();
        fetchConversations(0);
    }

    @Override
    public void onStart() {
        super.onStart();
        initSignedInUser();
    }

    @Override
    public void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkAnUnRegEventBus();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String message = (String) o;
                    switch (message) {
                        case AppConstants.DISABLE_NESTED_SCROLLING:
                            conversationsRecyclerView.setNestedScrollingEnabled(false);
                            break;
                        case AppConstants.ENABLE_NESTED_SCROLLING:
                            conversationsRecyclerView.setNestedScrollingEnabled(true);
                            break;
                    }
                }
            }
        });
    }

    private class ConversationsComparator implements Comparator<ParseObject> {

        @Override
        public int compare(ParseObject firstObject, ParseObject secondObject) {
            return Long.valueOf(firstObject.getLong(AppConstants.LAST_UPDATE_TIME)).compareTo(secondObject.getLong(AppConstants.LAST_UPDATE_TIME));
        }

    }

}