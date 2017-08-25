package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewFlipper;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.EndlessRecyclerViewScrollListener;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.adapters.ConversationsAdapter;
import com.wan.hollout.ui.helpers.DividerItemDecoration;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unchecked")
public class ConversationsFragment extends Fragment {

    @BindView(R.id.conversations_recycler_view)
    RecyclerView conversationsRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.no_feed_view)
    View noFeedView;

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.no_hollout_users_text_view)
    HolloutTextView errorTextView;

    private ConversationsAdapter conversationsAdapter;
    private List<ConversationItem> conversations = new ArrayList<>();
    private ParseObject signedInUser;

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = AuthUtil.getCurrentUser();
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
        View peopleView = inflater.inflate(R.layout.fragment_conversations, container, false);
        ButterKnife.bind(this, peopleView);
        return peopleView;
    }

    private void setupAdapter() {
        conversationsAdapter = new ConversationsAdapter(getActivity(), conversations);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        conversationsRecyclerView.setLayoutManager(linearLayoutManager);
        conversationsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        conversationsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
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
        attemptOffloadConversationsFromCache();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchConversations(0);
            }
        });
    }

    private void loadAdapter(List<ParseObject> users) {
        if (!users.isEmpty()) {
            for (ParseObject parseUser : users) {
                ConversationItem conversationItem = new ConversationItem(parseUser, HolloutPreferences.getLastConversationTime(parseUser.getString(AppConstants.REAL_OBJECT_ID)));
                if (!conversations.contains(conversationItem)) {
                    conversations.add(conversationItem);
                }
            }
            sortConversations();
        }
        conversationsAdapter.notifyDataSetChanged();
    }

    private void attemptOffloadConversationsFromCache() {
        ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.fromPin(AppConstants.CONVERSATIONS);
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.whereNotEqualTo(AppConstants.REAL_OBJECT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            peopleAndGroupsQuery.setLimit(100);
            peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects != null && !objects.isEmpty()) {
                        loadAdapter(objects);
                    }
                    invalidateEmptyView();
                    fetchConversations(0);
                }
            });
        }
    }

    private void cacheConversations() {
        ParseObject.unpinAllInBackground(AppConstants.CONVERSATIONS, new DeleteCallback() {

            @Override
            public void done(ParseException e) {
                List<ParseObject> refinedConversations = new ArrayList<>();
                for (ConversationItem conversationItem : conversations) {
                    refinedConversations.add(conversationItem.getRecipient());
                }
                ParseObject.pinAllInBackground(AppConstants.CONVERSATIONS, refinedConversations);
            }
        });
    }

    private void fetchConversations(final int skip) {
        ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);

        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.whereNotEqualTo(AppConstants.REAL_OBJECT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            peopleAndGroupsQuery.setLimit(100);
            if (skip != 0) {
                peopleAndGroupsQuery.setSkip(skip);
            }
            peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        if (objects != null && !objects.isEmpty()) {
                            if (skip == 0) {
                                conversations.clear();
                            }
                            sortConversations();
                            loadAdapter(objects);
                            if (!conversations.isEmpty()) {
                                cacheConversations();
                            }
                        } else {
                            if (skip == 0) {
                                showConversationEmptyViewAsNecessary(-1);
                            }
                        }
                        invalidateEmptyView();
                        swipeRefreshLayout.setRefreshing(false);
                    } else {
                        if (skip == 0) {
                            showConversationEmptyViewAsNecessary(e.getCode());
                        }
                    }
                }
            });
        }
    }

    private void showConversationEmptyViewAsNecessary(int errorCode) {
        if (conversations.isEmpty()) {
            UiUtils.toggleFlipperState(contentFlipper, 1);
        }
        if (errorCode == ParseException.CONNECTION_FAILED && getActivity()!=null) {
            errorTextView.setText(getString(R.string.network_error));
            errorTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (conversations.isEmpty()) {
                        UiUtils.blinkView(v);
                        UiUtils.toggleFlipperState(contentFlipper, 0);
                        fetchConversations(0);
                    }
                }
            });
        }
    }

    private void sortConversations() {
        Collections.sort(conversations);
    }

    private void invalidateEmptyView() {
        UiUtils.toggleFlipperState(contentFlipper, conversations.isEmpty() ? 0 : 2);
    }

    @Override
    public void onResume() {
        super.onResume();
        initSignedInUser();
        checkAndRegEventBus();
        invalidateAdapter();
        fetchConversations(0);
    }

    private void invalidateAdapter() {
        if (conversationsAdapter != null) {
            conversationsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initSignedInUser();
        invalidateAdapter();
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
                        case AppConstants.REFRESH_CONVERSATIONS:
                            fetchConversations(0);
                            break;
                    }
                }
            }
        });
    }
}
