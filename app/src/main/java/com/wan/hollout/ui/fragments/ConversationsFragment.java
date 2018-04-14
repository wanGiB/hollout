package com.wan.hollout.ui.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.wan.hollout.eventbuses.ConversationItemChangedEvent;
import com.wan.hollout.eventbuses.SearchChatsEvent;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.adapters.ConversationsAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.ConversationsList;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unchecked")
public class ConversationsFragment extends BaseFragment {

    @BindView(R.id.conversations_recycler_view)
    RecyclerView conversationsRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.content_flipper)
    ViewFlipper contentFlipper;

    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    @BindView(R.id.no_hollout_users_text_view)
    HolloutTextView errorTextView;

    private List<ConversationItem> conversations = new ArrayList<>();

    @SuppressLint("StaticFieldLeak")
    public static ConversationsAdapter conversationsAdapter;
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
        public void onModelChanged(@NonNull final ChatMessage model, @NonNull final BaseModel.Action action) {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Check for the model that just changed
                        if (conversationsAdapter.getItemCount() != 0) {
                            List<Integer> removableIndices = new ArrayList<>();

                            for (ConversationItem conversationItem : conversations) {
                                String conversationId = conversationItem.getObjectId();
                                if (model.getConversationId().equals(conversationId)) {
                                    int indexOfConversation = conversations.indexOf(conversationItem);
                                    if (indexOfConversation != -1) {
                                        if (action == BaseModel.Action.DELETE) {
                                            if (!removableIndices.contains(indexOfConversation)) {
                                                removableIndices.add(indexOfConversation);
                                            }
                                        } else {
                                            conversationsAdapter.notifyItemChanged(indexOfConversation);
                                        }
                                    }
                                }
                            }

                            if (!removableIndices.isEmpty()) {
                                for (Integer removableIndex : removableIndices) {
                                    conversations.remove(removableIndex.intValue());
                                    conversationsAdapter.notifyItemRemoved(removableIndex);
                                }
                                removableIndices.clear();
                            }

                        }
                    }
                });
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
                        if (conversationsAdapter.getItemCount() != 0) {
                            if (StringUtils.isNotEmpty(searchString)) {
                                searchChats(searchString, conversations.size());
                            } else {
                                if (conversations.size() >= 100) {
                                    fetchConversations(conversations.size());
                                }
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
                if (!parseUser.getString(AppConstants.REAL_OBJECT_ID)
                        .equals(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                    ConversationItem conversationItem = new ConversationItem(parseUser);
                    if (!conversations.contains(conversationItem)) {
                        conversations.add(conversationItem);
                    }
                }
            }
            sortConversations();
        }
        conversationsAdapter.notifyDataSetChanged();
    }

    private void attemptOffloadConversationsFromCache() {
        final ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.fromPin(AppConstants.CONVERSATIONS);
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.setLimit(100);
            peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (objects != null && !objects.isEmpty()) {
                        loadAdapter(objects);
                    }
                    invalidateEmptyView();
                    fetchConversations(0);
                    peopleAndGroupsQuery.cancel();
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
        final ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.setLimit(100);
            if (skip != 0) {
                peopleAndGroupsQuery.setSkip(skip);
            }
            peopleAndGroupsQuery.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    MainActivity.materialSearchView.hideProgressBar();
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
                    peopleAndGroupsQuery.cancel();
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
        if (!conversations.isEmpty()) {
            Collections.sort(conversations);
            HolloutLogger.d("SortedConversations", TextUtils.join(",", conversations));
        }
    }

    private void invalidateEmptyView() {
        UiUtils.toggleFlipperState(contentFlipper, conversations.isEmpty() ? 0 : 2);
    }

    private void refreshConversations() {
        if (!conversations.isEmpty()) {
            try {
                if (!ConversationsList.recentConversations().isEmpty()) {
                    for (ConversationItem recentConversationItem : ConversationsList.recentConversations()) {
                        if (!conversations.contains(recentConversationItem)) {
                            conversations.add(0, recentConversationItem);
                            conversationsAdapter.notifyItemInserted(0);
                        } else {
                            int indexOfItem = conversations.indexOf(recentConversationItem);
                            if (indexOfItem != -1) {
                                conversations.remove(indexOfItem);
                                conversationsAdapter.notifyItemRemoved(indexOfItem);
                                conversations.add(0, recentConversationItem);
                                sortConversations();
                                conversationsAdapter.notifyItemInserted(0);
                            }
                        }
                    }
                    ConversationsList.recentConversations().clear();
                }
            } catch (IndexOutOfBoundsException ignored) {
                conversationsAdapter.notifyDataSetChanged();
                ConversationsList.recentConversations().clear();
            }
        } else {
            fetchConversations(0);
        }
        if (AppConstants.CHAT_INVITATION_ACCEPTED) {
            fetchConversations(0);
            AppConstants.CHAT_INVITATION_ACCEPTED = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initSignedInUser();
        refreshConversations();
        DirectModelNotifier.get().registerForModelChanges(ChatMessage.class, onModelStateChangedListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        initSignedInUser();
        refreshConversations();
    }

    @Override
    public void onStop() {
        super.onStop();
        DirectModelNotifier.get().unregisterForModelChanges(ChatMessage.class, onModelStateChangedListener);
    }

    @Override
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
                        case AppConstants.SEARCH_VIEW_CLOSED:
                            conversations.clear();
                            conversationsAdapter.notifyDataSetChanged();
                            conversationsAdapter.setSearchString(null);
                            fetchConversations(0);
                            break;
                        case AppConstants.ORDER_CONVERSATIONS:
                            sortConversations();
                            if (conversationsAdapter != null) {
                                conversationsAdapter.notifyDataSetChanged();
                            }
                            break;
                        case AppConstants.CLEAR_ALL_CHANGED_INDICES:
                            if (!AppConstants.changedIndices.isEmpty()) {
                                for (Integer index : AppConstants.changedIndices) {
                                    conversationsAdapter.notifyItemChanged(index);
                                }
                                AppConstants.changedIndices.clear();
                            }
                            break;
                    }
                } else if (o instanceof SearchChatsEvent) {
                    SearchChatsEvent searchChatsEvent = (SearchChatsEvent) o;
                    String searchString = searchChatsEvent.getQueryString();
                    ConversationsFragment.this.searchString = searchString;
                    searchChats(searchString, 0);
                } else if (o instanceof ConversationItemChangedEvent) {
                    ConversationItemChangedEvent conversationItemChangedEvent = (ConversationItemChangedEvent) o;
                    ConversationItem conversationItem = new ConversationItem(conversationItemChangedEvent.getParseObject());
                    int indexOfConversation = conversations.indexOf(conversationItem);
                    if (indexOfConversation != -1) {
                        if (conversationItemChangedEvent.isDeleted()) {
                            conversationsAdapter.notifyItemRemoved(indexOfConversation);
                        } else {
                            conversationsAdapter.notifyItemChanged(indexOfConversation);
                            if (!AppConstants.changedIndices.contains(indexOfConversation)) {
                                AppConstants.changedIndices.add(indexOfConversation);
                            }
                        }
                    }
                }
                EventBus.getDefault().removeAllStickyEvents();
            }
        });
    }

    private void searchChats(String searchString, final int skip) {
        if (StringUtils.isEmpty(searchString)) {
            conversationsAdapter.setSearchString(null);
            fetchConversations(0);
            return;
        }
        conversationsAdapter.setSearchString(searchString);
        final ParseQuery<ParseObject> peopleAndGroupsQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (signedInUserChats != null && !signedInUserChats.isEmpty()) {
            peopleAndGroupsQuery.whereContainedIn(AppConstants.REAL_OBJECT_ID, signedInUserChats);
            peopleAndGroupsQuery.whereContains(AppConstants.SEARCH_CRITERIA, searchString.toLowerCase());
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
                    peopleAndGroupsQuery.cancel();
                }
            });
        } else {
            UiUtils.toggleFlipperState(contentFlipper, 1);
        }

    }

}