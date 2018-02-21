package com.wan.hollout.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.SearchChatsEvent;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.adapters.ConversationsAdapter;
import com.wan.hollout.ui.helpers.DividerItemDecoration;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
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

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    @BindView(R.id.no_hollout_users_text_view)
    HolloutTextView errorTextView;

    @SuppressLint("StaticFieldLeak")
    public static ConversationsAdapter conversationsAdapter;
    public static List<ConversationItem> conversations = new ArrayList<>();
    private ParseObject signedInUser;

    public String searchString;

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = AuthUtil.getCurrentUser();
        }
    }

    private DirectModelNotifier.ModelChangedListener onModelStateChangedListener = new DirectModelNotifier.ModelChangedListener<ChatMessage>() {

        @Override
        public void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action) {

        }

        @Override
        public void onModelChanged(@NonNull ChatMessage model, @NonNull BaseModel.Action action) {
            //Check for the model that just changed
            if (!conversations.isEmpty()) {
                for (ConversationItem conversationItem : conversations) {
                    String conversationId = conversationItem.getObjectId();
                    if (model.getConversationId().equals(conversationId)) {
                        int indexOfConversation = conversations.indexOf(conversationItem);
                        if (indexOfConversation != -1) {
                            conversationsAdapter.notifyItemChanged(indexOfConversation);
                        }
                    }
                }
            }
        }
    };


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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        conversationsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupAdapter() {
        conversationsAdapter = new ConversationsAdapter(getActivity(), conversations);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        conversationsRecyclerView.setLayoutManager(linearLayoutManager);
        conversationsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        conversationsRecyclerView.setAdapter(conversationsAdapter);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1) != null) {
                    if ((scrollY >= (nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        if (!conversations.isEmpty()) {
                            if (StringUtils.isNotEmpty(searchString)) {
                                searchChats(searchString, conversations.size());
                            } else {
                                fetchConversations(conversations.size());
                            }
                        }
                    }
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
        } else {
            UiUtils.toggleFlipperState(contentFlipper, 1);
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
        } else {
            UiUtils.toggleFlipperState(contentFlipper, 1);
        }
    }

    private void showConversationEmptyViewAsNecessary(int errorCode) {
        if (conversations.isEmpty()) {
            UiUtils.toggleFlipperState(contentFlipper, 1);
        }
        if (errorCode == ParseException.CONNECTION_FAILED && getActivity() != null) {
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
        refreshConversations();
        invalidateAdapter();
        AppConstants.recentConversations.clear();
    }

    private void refreshConversations() {
        if (!AppConstants.recentConversations.isEmpty()) {
            for (ParseObject parseObject : AppConstants.recentConversations) {
                ConversationItem conversationItem = new ConversationItem(parseObject,
                        HolloutPreferences.getLastConversationTime(parseObject.getString(AppConstants.REAL_OBJECT_ID)));
                if (conversations.isEmpty()) {
                    conversations.add(conversationItem);
                } else {
                    int indexOfConversationItem = conversations.indexOf(conversationItem);
                    if (indexOfConversationItem == -1) {
                        conversations.add(0, conversationItem);
                    } else {
                        Collections.swap(conversations, 0, indexOfConversationItem);
                    }
                }
            }
            invalidateEmptyView();
        }
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
        DirectModelNotifier.get().registerForModelChanges(ChatMessage.class, onModelStateChangedListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
        DirectModelNotifier.get().unregisterForModelChanges(ChatMessage.class, onModelStateChangedListener);
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
                            nestedScrollView.setNestedScrollingEnabled(false);
                            break;
                        case AppConstants.ENABLE_NESTED_SCROLLING:
                            nestedScrollView.setNestedScrollingEnabled(true);
                            break;
                        case AppConstants.REFRESH_CONVERSATIONS:
                            fetchConversations(0);
                            break;
                    }
                } else if (o instanceof SearchChatsEvent) {
                    SearchChatsEvent searchChatsEvent = (SearchChatsEvent) o;
                    String searchString = searchChatsEvent.getQueryString();
                    ConversationsFragment.this.searchString = searchString;
                    searchChats(searchString, 0);
                }
                EventBus.getDefault().removeAllStickyEvents();
            }
        });
    }

    private void searchChats(String searchString, final int skip) {
        if (StringUtils.isNotEmpty(searchString)) {
            conversationsAdapter.setSearchString(searchString);
        } else {
            conversationsAdapter.setSearchString(null);
        }
        ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.whereNotEqualTo(AppConstants.REAL_OBJECT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            peopleAndGroupsQuery.whereContains(AppConstants.APP_USER_DISPLAY_NAME, searchString.toLowerCase());
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
        } else {
            UiUtils.toggleFlipperState(contentFlipper, 1);
        }
    }

}